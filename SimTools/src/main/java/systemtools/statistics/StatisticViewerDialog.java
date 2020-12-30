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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.function.IntConsumer;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import systemtools.BaseDialog;
import systemtools.images.SimToolsImages;

/**
 * Dialog zur exklusiven Anzeige eines Viewers außerhalb des normalen Statistik-Panels.
 * @author Alexander Herzog
 * @see StatisticsBasePanel
 */
public class StatisticViewerDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID=-2709040627977308191L;

	/**
	 * Standardexportgröße für Bilder
	 */
	private final int defaultExportSize;

	/**
	 * Callback zum Einstellen einer neuen Standardexportgröße für Bilder
	 */
	private final IntConsumer setImageSize;

	/**
	 * Konstruktor der Klasse
	 * @param owner	Übergeordnetes Element
	 * @param viewer	In dem Dialog anzuzeigender Viewer
	 * @param defaultExportSize	Standardexportgröße für Bilder
	 * @param setImageSize	Callback zum Einstellen einer neuen Standardexportgröße für Bilder
	 */
	public StatisticViewerDialog(final Component owner, final StatisticViewer viewer, final int defaultExportSize, final IntConsumer setImageSize) {
		super(owner,StatisticsBasePanel.viewersToolbarNewWindowTitle);
		this.defaultExportSize=defaultExportSize;
		this.setImageSize=setImageSize;

		/* GUI */
		showCloseButton=true;
		final JPanel content=createGUI(null);
		content.setLayout(new BorderLayout());

		/* Toolbar */
		final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		JButton button;

		if (viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_UNZOOM)) {
			toolbar.add(button=new JButton(StatisticsBasePanel.viewersToolbarZoom,SimToolsImages.ZOOM.getIcon()));
			button.setToolTipText(StatisticsBasePanel.viewersToolbarZoomHint);
			button.addActionListener(e->viewer.unZoom());
		}
		if (viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_COPY)) {
			toolbar.add(button=new JButton(StatisticsBasePanel.viewersToolbarCopy,SimToolsImages.COPY.getIcon()));
			button.setToolTipText(StatisticsBasePanel.viewersToolbarCopyHint);
			button.addActionListener(e->copyViewer((JButton)e.getSource(),viewer));
		}
		if (viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_PRINT)) {
			toolbar.add(button=new JButton(StatisticsBasePanel.viewersToolbarPrint,SimToolsImages.PRINT.getIcon()));
			button.setToolTipText(StatisticsBasePanel.viewersToolbarPrintHint);
			button.addActionListener(e->viewer.print());
		}
		if (viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_SAVE)) {
			toolbar.add(button=new JButton(StatisticsBasePanel.viewersToolbarSave,SimToolsImages.SAVE.getIcon()));
			button.setToolTipText(StatisticsBasePanel.viewersToolbarSaveHint);
			button.addActionListener(e->saveViewer((JButton)e.getSource(),viewer));
		}
		if (viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_SEARCH)) {
			toolbar.add(button=new JButton(StatisticsBasePanel.viewersToolbarSearch,SimToolsImages.SEARCH.getIcon()));
			button.setToolTipText(StatisticsBasePanel.viewersToolbarSearchHint);
			button.addActionListener(e->viewer.search(this));
		}
		final JButton[] additionalButtons=viewer.getAdditionalButton();
		if (additionalButtons!=null) for (JButton b: additionalButtons) toolbar.add(b);

		toolbar.add(Box.createHorizontalGlue());

		toolbar.add(button=new JButton(StatisticsBasePanel.viewersToolbarWindowSize,SimToolsImages.FULLSCREEN.getIcon()));
		button.setToolTipText(StatisticsBasePanel.viewersToolbarWindowSizeHint);
		button.addActionListener(e->{
			final JButton b=(JButton)e.getSource();
			final JPopupMenu menu=new JPopupMenu();
			menu.add(getSizeItem(800,600));
			menu.add(getSizeItem(1024,768));
			menu.add(getSizeItem(1280,720));
			menu.add(getSizeItem(1440,810));
			menu.add(getSizeItem(1920,1080));
			menu.addSeparator();
			menu.add(getSizeItem(-1,-1));
			menu.show(b,0,b.getHeight());
		});

		/* Viewer */
		content.add(viewer.getViewer(false),BorderLayout.CENTER);

		/* Kopier-Hotkeys setzen */
		if (viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_COPY)) {
			final JRootPane root=getRootPane();

			SwingUtilities.invokeLater(()->{
				final KeyStroke keyCtrlC=KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK,true); /* true=Beim Loslassen erkennen; muss gesetzt sein, da die Subviewer die anderen Hotkeys teilweise aufhalten */
				final KeyStroke keyCtrlIns=KeyStroke.getKeyStroke(KeyEvent.VK_INSERT,InputEvent.CTRL_DOWN_MASK,true);  /* true=Beim Loslassen erkennen; muss gesetzt sein, da die Subviewer die anderen Hotkeys teilweise aufhalten */
				root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyCtrlC,"CopyViewer");
				root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyCtrlIns,"CopyViewer");
			});

			root.getActionMap().put("CopyViewer",new AbstractAction() {
				private static final long serialVersionUID=8765042416268087967L;
				@Override
				public void actionPerformed(ActionEvent e) {
					if (!viewer.getCanDo(StatisticViewer.CanDoAction.CAN_DO_COPY)) return;
					viewer.copyToClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
				}
			});
		}

		/* Dialog starten */
		setResizable(true);
		setMinSizeRespectingScreensize(640,480);
		setLocationRelativeTo(getOwner());
		setVisible(true);
	}

	/**
	 * Erzeugt einen Menüpunkt zur Wahl einer Fenstergröße.
	 * @param width	Einzustellende Fensterbreite (Werte kleiner als 0 für Vollbild)
	 * @param height	Einzustellende Fensterhöhe (Werte kleiner als 0 für Vollbild)
	 * @return	Neuer Menüpunkt
	 */
	private JMenuItem getSizeItem(final int width, final int height) {
		JMenuItem item;
		if (width<0 || height<0) {
			item=new JMenuItem(StatisticsBasePanel.viewersToolbarFullscreen,SimToolsImages.FULLSCREEN.getIcon());
			item.setToolTipText(StatisticsBasePanel.viewersToolbarFullscreenHint);
			item.addActionListener(e->{
				final Dimension screenSize=Toolkit.getDefaultToolkit().getScreenSize();
				final Insets border=getInsets();
				final int wPlus=border.left+border.right;
				setBounds(-border.left,0,screenSize.width+wPlus,screenSize.height);
				toFront();
			});
		} else {
			item=new JMenuItem(width+"x"+height);
			item.addActionListener(e->{
				setSize(width,height);
				setLocationRelativeTo(getOwner());
			});
		}
		return item;
	}

	/**
	 * Kopiert den Viewer in der angegebenen Größe in die Zwischenablage.
	 * @param viewer	Zu kopierender Viewer
	 * @param size	Größe (-1 für Standardexportgröße)
	 */
	private void copyViewer(final StatisticViewer viewer, final int size) {
		if (size>=0) setImageSize.accept(size);
		viewer.copyToClipboard(Toolkit.getDefaultToolkit().getSystemClipboard());
		if (size>=0) setImageSize.accept(defaultExportSize);
	}

	/**
	 * Speichert den Inhalt des Viewers in der angegebenen Größe in einer Datei.
	 * @param viewer	Zu speichernder Viewer
	 * @param size	Größe (-1 für Standardexportgröße)
	 */
	private void saveViewer(final StatisticViewer viewer, final int size) {
		if (size>=0) setImageSize.accept(size);
		viewer.save(this);
		if (size>=0) setImageSize.accept(defaultExportSize);
	}

	/**
	 * Liefert die Exportgröße gemäß Fenstergröße.
	 * @return	Exportgröße gemäß Fenstergröße
	 */
	private int getScreenExportSize() {
		return Math.max(getWidth(),getHeight());
	}

	/**
	 * Kopiert den Viewer in die Zwischenablage.
	 * @param sender	Auslösendes Button (zur Ausrichtung des Popupmenüs, wenn nötig)
	 * @param viewer	Zu kopierender Viewer
	 */
	private void copyViewer(final JButton sender, final StatisticViewer viewer) {
		if (viewer instanceof StatisticViewerJFreeChart) {
			final int screenExportSize=getScreenExportSize();
			final JPopupMenu menu=new JPopupMenu();
			JMenuItem item;
			menu.add(item=new JMenuItem(String.format(StatisticsBasePanel.viewersToolbarCopyDefaultSize,defaultExportSize,defaultExportSize),SimToolsImages.COPY.getIcon()));
			item.addActionListener(e->copyViewer(viewer,-1));
			menu.add(item=new JMenuItem(String.format(StatisticsBasePanel.viewersToolbarCopyWindowSize,screenExportSize,screenExportSize),SimToolsImages.FULLSCREEN.getIcon()));
			item.addActionListener(e->copyViewer(viewer,screenExportSize));
			menu.show(sender,0,sender.getHeight());

		} else {
			copyViewer(viewer,-1);
		}
	}

	/**
	 * Speichert den Inhalt des Viewers in der angegebenen Größe in einer Datei.
	 * @param sender	Auslösendes Button (zur Ausrichtung des Popupmenüs, wenn nötig)
	 * @param viewer	Zu speichernder Viewer
	 */
	private void saveViewer(final JButton sender, final StatisticViewer viewer) {
		if (viewer instanceof StatisticViewerJFreeChart) {
			final int screenExportSize=getScreenExportSize();
			final JPopupMenu menu=new JPopupMenu();
			JMenuItem item;
			menu.add(item=new JMenuItem(String.format(StatisticsBasePanel.viewersToolbarSaveDefaultSize,defaultExportSize,defaultExportSize),SimToolsImages.SAVE.getIcon()));
			item.addActionListener(e->saveViewer(viewer,-1));
			menu.add(item=new JMenuItem(String.format(StatisticsBasePanel.viewersToolbarSaveWindowSize,screenExportSize,screenExportSize),SimToolsImages.FULLSCREEN.getIcon()));
			item.addActionListener(e->saveViewer(viewer,screenExportSize));
			menu.show(sender,0,sender.getHeight());

		} else {
			saveViewer(viewer,-1);
		}
	}
}
