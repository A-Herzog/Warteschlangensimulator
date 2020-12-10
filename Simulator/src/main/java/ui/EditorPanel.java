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
package ui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.NumberTools;
import mathtools.TimeTools;
import mathtools.distribution.swing.CommonVariables;
import mathtools.distribution.tools.FileDropperData;
import simulator.coreelements.RunElement;
import simulator.editmodel.EditModel;
import simulator.statistics.Statistics;
import statistics.StatisticsDataPerformanceIndicator;
import statistics.StatisticsQuotientPerformanceIndicator;
import statistics.StatisticsSimpleCountPerformanceIndicator;
import statistics.StatisticsTimePerformanceIndicator;
import systemtools.BaseDialog;
import systemtools.ImageTools;
import systemtools.MsgBox;
import tools.ButtonRotator;
import tools.SetupData;
import tools.SlidesGenerator;
import ui.dialogs.BackgroundColorDialog;
import ui.dialogs.LayersDialog;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.DrawIOExport;
import ui.modeleditor.ModelElementCatalog;
import ui.modeleditor.ModelElementCatalogListCellRenderer;
import ui.modeleditor.ModelElementCatalogTransferHandler;
import ui.modeleditor.ModelElementNavigatorListCellRenderer;
import ui.modeleditor.ModelResource;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.ModelSurfacePanel;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementListGroup;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.elements.ModelElementAssign;
import ui.modeleditor.elements.ModelElementBatch;
import ui.modeleditor.elements.ModelElementCounter;
import ui.modeleditor.elements.ModelElementCounterBatch;
import ui.modeleditor.elements.ModelElementDelay;
import ui.modeleditor.elements.ModelElementDifferentialCounter;
import ui.modeleditor.elements.ModelElementMatch;
import ui.modeleditor.elements.ModelElementProcess;
import ui.modeleditor.elements.ModelElementSource;
import ui.modeleditor.elements.ModelElementThroughput;
import ui.modeleditor.outputbuilder.HTMLOutputBuilder;
import ui.modelproperties.ModelPropertiesDialog;
import ui.parameterseries.ParameterCompareTemplatesDialog;
import ui.quickaccess.JPlaceholderTextField;
import ui.speedup.BackgroundSystem;
import ui.statistics.StatisticTools;

/**
 * Diese Klasse kapselt einen vollständigen Editor für ein <code>EditModel</code>-Objekt
 * @see EditorPanelBase
 * @author Alexander Herzog
 */
public final class EditorPanel extends EditorPanelBase {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 871808238984135272L;

	/**
	 * Wird am Ende des Konstruktors auf <code>true</code> gesetzt und stellt sicher,
	 * dass vorher noch keine Listener benachrichtigt werden.
	 * @see #fireTemplatesVisibleChanged()
	 * @see #fireNavigatorVisibleChanged()
	 */
	private boolean guiReady=false;

	/**
	 * Schaltet einige Funktionen zum Bearbeiten auch während des Read-Only-Modus frei.<br>
	 * Erlaubt wird das Bearbeiten von Elementen sowie das Verschieben.
	 * Das Löschen von Elementen und das Erstellen neuer Elemente bleibt aber verboten.
	 * @see #allowEditorDialogs()
	 */
	private boolean allowChangeOperationsOnReadOnly=false;

	/* Angaben zum Modell */

	/**
	 * Name des Modells
	 * @see EditModel#name
	 */
	private String name;

	/**
	 * Beschreibung des Modells
	 * @see EditModel#description
	 */
	private String description;

	/**
	 * Autor des Modells
	 * @see EditModel#author
	 */
	private String author;

	/**
	 * Anzahl der zu simulierenden Kundenankünfte
	 * @see EditModel#clientCount
	 */
	private long clientCount;

	/**
	 * Länge der Einschwingphase (als Anteil der Kundenankünfte), bevor die Statistikzählung beginnt.
	 * Die Einschwingphase wird nicht von der Kundenanzahl abgezogen, sondern besteht aus zusätzlichen Ankünften.
	 * @see EditModel#warmUpTime
	 */
	private double warmUpTime;

	/* Allgemeines zur GUI */

	/** Übergeordnetes Element */
	private final Component owner;

	/** Listener für Klicks auf die verschiedenen Symbolleisten-Schaltflächen */
	private ToolbarListener toolbarListener;

	/**
	 * Callback welches im Bedarfsfall (zur Anzeige von Tooltipdaten) ein Statistik-Objekt liefert
	 * @see #setStatisticsGetter(Supplier)
	 * @see #getStatisticsInfoForElement(ModelElementBox)
	 */
	private Supplier<Statistics> statisticsGetter;

	/**
	 * Callback-Methode, die aufgerufen werden soll, wenn in der Modell-Übersicht auf "Suchen" geklickt wird
	 * @see #setElementSearchCallback(Runnable)
	 */
	private Runnable callbackElementSearch;

	/**
	 * Callback-Methode, die aufgerufen werden soll, wenn in der Modell-Übersicht auf "Elementeliste" geklickt wird
	 * @see #setElementListCallback(Runnable)
	 */
	private Runnable callbackElementList;

	/* Infobereich oben */

	/** Infozeile oben */
	private JToolBar additionalInfoArea;
	/** Textfeld in der Infozeile oben */
	private JLabel additionalInfoLabel;

	/* Vertikale Symbolleiste */

	/** Schaltfläche "Modell" */
	private JButton buttonProperties;
	/** Schaltfläche "Element" (hinzufügen) */
	private JButton buttonTemplates;
	/** Schaltfläche "Kante" (hinzufügen) */
	private JButton buttonAddEdge;
	/** Separator vor den Rückgängig/Wiederholen-Schaltflächen */
	private JToolBar.Separator separatorUndoRedo;
	/** Schaltfläche "Rückgängig" */
	private JButton buttonUndo;
	/** Schaltfläche "Wiederholen" */
	private JButton buttonRedo;
	/** Schaltfläche "Überblick" (Modell-Explorer) */
	private JButton buttonExplorer;

	/* Statusleiste */

	/** Statusinformationen */
	private JLabel statusBar;
	/** Aktueller Zoomfaktor */
	private JLabel labelZoom;
	/** Schaltfläche für Zoomfaktor verringern */
	private JButton buttonZoomOut;
	/** Schaltfläche für Zoomfaktor vergrößern */
	private JButton buttonZoomIn;
	/** Schaltfläche für Standard-Zoomfaktor */
	private JButton buttonZoomDefault;
	/** Schaltfläche "Modell zentrieren" */
	private JButton buttonFindModel;

	/** Zeichenfläche */
	private ModelSurfacePanel surfacePanel;
	/** Lineale */
	private RulerPanel rulerPanel;

	/* Ausklapp-Panel links */

	/** Panel links von der Zeichenfläche */
	private JPanel leftArea;
	/** Listendarstellung der Vorlagen */
	private JList<ModelElementPosition> templates;
	/** Renderer für die Listendarstellung der Vorlagen */
	private ModelElementCatalogListCellRenderer<ModelElementPosition> templatesRenderer;
	/** Vorlagen-Bereich innerhalb des Panel links */
	private JPanel leftAreaTemplates;
	/** Schnellfilter für die Vorlagenliste */
	private JPlaceholderTextField leftAreaQuickFilter;
	/** Popupmenü-Schaltfläche für die Vorlagenliste */
	private JButton leftAreaTemplatesFilterButton;

	/* Ausklapp-Panel links */

	/** Panel rechts von der Zeichenfläche */
	private JPanel rightArea;
	/** Listendarstellung der Elemente im Modell */
	private JList<ModelElementBox> navigator;
	/** Datenmodell für die Listendarstellung der Elemente im Modell */
	private DefaultListModel<ModelElementBox> navigatorModel;
	/** Renderer für die Listendarstellung der Elemente im Modell */
	private ModelElementNavigatorListCellRenderer<ModelElementBox> navigatorRenderer;

	/**
	 * Konstruktor der Klasse <code>EditorPanel</code>
	 * @param owner	Übergeordnetes Element
	 * @param model	Anzuzeigendes Modell
	 * @param readOnly	Wird hier <code>true</code> angegeben, so kann das Modell nicht verändert werden.
	 * @param showEditModelProperties	Gibt an, ob die Schaltfläche zum Bearbeiten der Modelleigenschaften sichtbar sein soll
	 * @param canUndo	Gibt an, ob das Undo/Redo-System für dieses EditorPanel aktiv sein soll (bei Unterelementen sollte es deaktiviert werden)
	 */
	public EditorPanel(final Component owner, final EditModel model, final boolean readOnly, final boolean showEditModelProperties, final boolean canUndo) {
		super(model,readOnly);
		this.owner=(owner==null)?this:owner;
		buttonProperties.setVisible(showEditModelProperties);
		surfacePanel.setShowEditModelProperties(showEditModelProperties);
		if (!canUndo) {
			surfacePanel.disableUndo();
			if (separatorUndoRedo!=null) separatorUndoRedo.setVisible(false);
			if (buttonUndo!=null) buttonUndo.setVisible(false);
			if (buttonRedo!=null) buttonRedo.setVisible(false);
		}

		guiReady=true;
		fireTemplatesVisibleChanged();
	}

	/**
	 * Konstruktor der Klasse<br>
	 * Erstellt ein leeres {@link EditorPanel}.
	 * @param owner	Übergeordnetes Element
	 */
	public EditorPanel(final Component owner) {
		super();
		this.owner=(owner==null)?this:owner;
		guiReady=true;
		fireTemplatesVisibleChanged();
	}

	@Override
	public void paint(Graphics g) {
		setupInfoLabels(false);
		super.paint(g);
	}

	/**
	 * Wurden die Info-Labels auf der Zeichenfläche bereits einmal
	 * deaktiviert?<br>
	 * (Im Modus "nur am Anfang anzeigen" werden sie dann nicht
	 * wieder aktiviert.)
	 * @see #setupInfoLabels(boolean)
	 */
	private boolean infoLabelsDone=false;

	/**
	 * Aktiviert oder deaktiviert die Info-Labels auf der Zeichenfläche.
	 * @param turnOff	Info-Labels aktivieren (<code>false</code>) oder deaktivieren (<code>true</code>)
	 */
	private void setupInfoLabels(final boolean turnOff) {
		if (surfacePanel==null) return;
		final SetupData.SurfaceHelp surfaceHelp=SetupData.getSetup().surfaceHelp;

		if (!infoLabelsDone && !turnOff && surfaceHelp!=SetupData.SurfaceHelp.NEVER && buttonProperties!=null && buttonTemplates!=null && buttonAddEdge!=null && !leftAreaTemplates.isVisible()) {
			surfacePanel.setInfoPositions(new int[]{
					buttonProperties.getY()+buttonProperties.getHeight()/2,
					buttonTemplates.getY()+buttonTemplates.getHeight()/2,
					buttonAddEdge.getY()+buttonAddEdge.getHeight()/2
			});
		} else {
			surfacePanel.setInfoPositions(null);
			if (turnOff && surfaceHelp==SetupData.SurfaceHelp.START_ONLY) infoLabelsDone=true;
		}
	}

	/**
	 * Gibt an, ob ein Raster auf der Zeichenfläche angezeigt wird
	 * @return	Rasteranzeige
	 */
	public ModelSurface.Grid getRaster() {
		return surfacePanel.getRaster();
	}

	/**
	 * Stellt ein, ob ein Raster auf der Zeichenfläche angezeigt wird
	 * @param raster	Rasteranzeige
	 */
	public void setRaster(final ModelSurface.Grid raster) {
		surfacePanel.setRaster(raster);
	}

	/**
	 * Verringert den Zoomfaktor.
	 */
	public void zoomOut() {
		surfacePanel.zoomOut();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Vergrößert den Zoomfaktor.
	 */
	public void zoomIn() {
		surfacePanel.zoomIn();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Stellt den Standard-Zoomfaktor wieder her.
	 */
	public void zoomDefault() {
		surfacePanel.zoomDefault();
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Liefert den aktuell eingestellten Zoomfaktor
	 * @return Aktueller Zoomfaktor
	 */
	public double getZoom() {
		return surfacePanel.getZoom();
	}

	/**
	 * Stellt den Zoomfaktor ein
	 * @param zoom	Neuer Zoomfaktor
	 */
	public void setZoom(final double zoom) {
		surfacePanel.setZoom(zoom);
		labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
	}

	/**
	 * Zentriert das Modell auf der Zeichenfläche.
	 */
	public void centerModel() {
		surfacePanel.centerModel();
	}

	/**
	 * Ist die Abweichung zwischen zentriertem Modell und dem
	 * Modell im Status nach oben links gescrollt pro Richtung kleiner
	 * als der angegeben Wert, so wird bei {@link #smartCenterModel()}
	 * nach oben links gescrollt statt das Modell tatsächlich
	 * zu zentrieren.
	 * @see #smartCenterModel()
	 */
	private static final int MAX_CENTER_DELTA=100;

	/**
	 * Zentriert das Modell auf der Zeichenfläche.<br>
	 * Bei nur kleinen Abweichungen von der linken oberen Ecke wird zu dieser gescrollt.
	 */
	public void smartCenterModel() {
		surfacePanel.centerModel();
		final Point top=surfacePanel.getTopPosition();
		final double zoom=surfacePanel.getZoom();
		final int maxDelta=(int)Math.round(MAX_CENTER_DELTA*zoom);
		if (top!=null && (top.x>0 || top.y>0)) {
			if (top.x<=maxDelta && top.y<=maxDelta) surfacePanel.scrollToTop();
		}
	}

	/**
	 * Scrollt ganz nach oben links.
	 */
	public void scrollToTop() {
		surfacePanel.scrollToTop();
	}

	/**
	 * Liefert die Position der linken oberen Ecke des sichtbaren Bereichs
	 * @return	Position der linken oberen Ecke des sichtbaren Bereichs
	 */
	public Point getPosition() {
		if (!(surfacePanel.getParent() instanceof JViewport)) return new Point(0,0);
		return ((JViewport)surfacePanel.getParent()).getViewPosition();
	}

	/**
	 * Gibt an, ob die Elemente-hinzufüge-Leiste angezeigt wird
	 * @return	Gibt <code>true</code> zurück, wenn die Elemente-hinzufüge-Leiste angezeigt wird
	 */
	public boolean isTemplatesVisible() {
		return leftAreaTemplates.isVisible();
	}

	/**
	 * Gibt an, ob die Navigator-Leiste angezeigt wird
	 * @return	Gibt <code>true</code> zurück, wenn die Navigator-Leiste angezeigt wird
	 */
	public boolean isNavigatorVisible() {
		return rightArea.isVisible();
	}

	/**
	 * Gibt an, ob die Funktion zum Hinzufügen von Kanten momentan aktiv ist
	 * @return	Gibt <code>true</code> zurück, wenn die Funktion zum Hinzufügen von Kanten momentan aktiv ist
	 */
	public boolean isAddEdgeActive() {
		return surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP1 || surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP2;
	}

	/**
	 * Listener, die beim Ändern des Sichtbarkeitsstatus der Vorlagenleiste benachrichtigt werden sollen
	 * @see #fireTemplatesVisibleChanged()
	 */
	private final Set<Runnable> templatesVisibleChangeListeners=new HashSet<>();

	/**
	 * Fügt einen Listener zu der Liste der Listener, die beim Ändern des Sichtbarkeitsstatus der Vorlagenleiste benachrichtigt werden sollen, hin.
	 * @param listener	Zukünftig zusätzlich zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener zu der Liste hinzugefügt werden konnte. Wenn er bereits in der in der Liste enthalten war, liefert die Funktion <code>false</code>.
	 */
	public boolean addTemplatesVisibleChangeListener(final Runnable listener) {
		return templatesVisibleChangeListeners.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die beim Ändern des Sichtbarkeitsstatus der Vorlagenleiste benachrichtigt werden sollen.
	 * @param listener	Zukünftig nicht mehr zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener aus der Liste entfernt werden konnte. Wenn er nicht enthalten war, liefert die Funktion <code>false</code>.
	 */
	public boolean removeTemplatesVisibleChangeListener(final Runnable listener) {
		return templatesVisibleChangeListeners.remove(listener);
	}

	/**
	 * Löst die Listener aus, die beim Ändern des Sichtbarkeitsstatus der Vorlagenleiste benachrichtigt werden sollen.
	 * @see #templatesVisibleChangeListeners
	 */
	private void fireTemplatesVisibleChanged() {
		if (templatesVisibleChangeListeners==null || !guiReady) return;
		for (Runnable listener: templatesVisibleChangeListeners)
			if (listener!=null) listener.run();
	}

	/**
	 * Stellt ein, ob die Elemente-hinzufüge-Leiste angezeigt werden soll
	 * @param templatesVisible	Elemente-hinzufüge-Leiste anzeigen
	 */
	public void setTemplatesVisible(final boolean templatesVisible) {
		leftAreaTemplates.setVisible(templatesVisible);
		leftArea.setVisible(false);
		leftArea.setVisible(true);

		if (!readOnly) {
			final SetupData setup=SetupData.getSetup();
			setup.showTemplates=isTemplatesVisible();
			setup.saveSetup();
		}

		setupInfoLabels(templatesVisible);

		fireTemplatesVisibleChanged();
	}

	/**
	 * Wählt ein Element in der Vorlagenleiste aus (und macht diese ggf. vorher sichtbar)
	 * @param element	Zu selektierendes Element
	 * @return	Liefert <code>true</code>, wenn das Element selektiert werden konnte. (Ein Grund für <code>false</code> ist, dass die betreffende Gruppe nicht ausgeklappt ist.)
	 * @see #selectTemplateInList(ModelElementPosition)
	 */
	private boolean selectTemplateInListDirect(final ModelElementPosition element) {
		if (element==null) return true;
		final String className=element.getClass().getName();
		setTemplatesVisible(true);
		final ListModel<ModelElementPosition> model=templates.getModel();
		final int count=model.getSize();
		boolean groupOpen=true;
		for (int i=0;i<count;i++) {
			final ModelElementPosition e=model.getElementAt(i);
			if (e instanceof ModelElementListGroup) {
				groupOpen=((ModelElementListGroup)e).isShowSub();
				continue;
			}
			if (!groupOpen) continue;
			if (e.getClass().getName().equals(className)) {
				templates.setSelectedIndex(i);
				templates.ensureIndexIsVisible(i);
				return true;
			}
		}
		return false;
	}

	/**
	 * Expandiert eine Vorlagengruppe, die ein bestimmtes Element enthält
	 * @param element	Element dessen Vorlagengruppe expandiert werden soll
	 * @see #selectTemplateInList(ModelElementPosition)
	 */
	private void expandGroupForElement(final ModelElementPosition element) {
		ListModel<ModelElementPosition> model=templates.getModel();
		final int count=model.getSize();
		for (int i=0;i<count;i++) {
			final ModelElementPosition e=model.getElementAt(i);
			if (e instanceof ModelElementListGroup) {
				final ModelElementListGroup group=(ModelElementListGroup)e;
				if (group.isInGroup(element) && !group.isShowSub()) {
					group.toggleShowSub();
					saveGroupOpenStatus(group.getIndex(),true);
					break;
				}
			}
		}

		updateTemplatesFilter();
		model=templates.getModel();
		templates.setModel(new DefaultListModel<>());
		templates.setModel(model);
		setTemplatesVisible(isTemplatesVisible());
	}

	/**
	 * Wählt ein Element in der Vorlagenleiste aus (und macht diese ggf. vorher sichtbar)
	 * @param element	Zu selektierendes Element
	 */
	public void selectTemplateInList(final ModelElementPosition element) {
		if (selectTemplateInListDirect(element)) return;
		expandGroupForElement(element);
		selectTemplateInListDirect(element);
	}

	/**
	 * Listener, die beim Ändern des Sichtbarkeitsstatus des Navigators benachrichtigt werden sollen
	 * @see #fireNavigatorVisibleChanged()
	 */
	private final Set<Runnable> navigatorVisibleChangeListeners=new HashSet<>();

	/**
	 * Fügt einen Listener zu der Liste der Listener, die beim Ändern des Sichtbarkeitsstatus des Navigators benachrichtigt werden sollen, hin.
	 * @param listener	Zukünftig zusätzlich zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener zu der Liste hinzugefügt werden konnte. Wenn er bereits in der in der Liste enthalten war, liefert die Funktion <code>false</code>.
	 */
	public boolean addNavigatorVisibleChangeListener(final Runnable listener) {
		return navigatorVisibleChangeListeners.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die beim Ändern des Sichtbarkeitsstatus des Navigators benachrichtigt werden sollen.
	 * @param listener	Zukünftig nicht mehr zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener aus der Liste entfernt werden konnte. Wenn er nicht enthalten war, liefert die Funktion <code>false</code>.
	 */
	public boolean removeNavigatorVisibleChangeListener(final Runnable listener) {
		return navigatorVisibleChangeListeners.remove(listener);
	}

	/**
	 * Löst die Listener, die beim Ändern des Sichtbarkeitsstatus des Navigators benachrichtigt werden sollen, aus.
	 * @see #navigatorVisibleChangeListeners
	 */
	private void fireNavigatorVisibleChanged() {
		if (navigatorVisibleChangeListeners==null || !guiReady) return;
		for (Runnable listener: navigatorVisibleChangeListeners)
			if (listener!=null) listener.run();
	}

	/**
	 * Stellt ein, ob die Navigator-Leiste angezeigt werden soll
	 * @param navigatorVisible	Navigator-Leiste anzeigen
	 */
	public void setNavigatorVisible(final boolean navigatorVisible) {
		rightArea.setVisible(navigatorVisible);
		fireNavigatorVisibleChanged();
	}

	/**
	 * Erzeugt eine Schaltfläche mit um 90° gegen den Uhrzeigersinn rotierter Beschriftung.
	 * @param toolbar	Symbolleiste in die die neue Schaltfläche eingefügt werden soll (kann <code>null</code> sein, dann wird die Schaltfläche in keine Symbolleiste eingefügt)
	 * @param title	Beschriftung der Schaltfläche (darf nicht leer sein)
	 * @param hint	Tooltip für die Schaltfläche (kann <code>null</code> sein)
	 * @param icon	Icon für die Schaltfläche (kann <code>null</code> sein)
	 * @return	Neue Schaltfläche
	 */
	private JButton createRotatedToolbarButton(final JToolBar toolbar, final String title, final String hint, final Icon icon) {
		ImageIcon rotatedIcon=null;

		if (icon instanceof ImageIcon) {
			final double scale=SetupData.getSetup().scaleGUI;
			if (scale!=1.0) {
				final int w=(int)Math.round(icon.getIconWidth()*scale);
				final int h=(int)Math.round(icon.getIconHeight()*scale);
				final Image temp=((ImageIcon)icon).getImage().getScaledInstance(w,h,Image.SCALE_SMOOTH);
				rotatedIcon=new ImageIcon(temp,"");
			} else {
				rotatedIcon=(ImageIcon)icon;
			}
		}

		final JButton button=new ButtonRotator.RotatedButton(title,rotatedIcon);

		if (toolbar!=null) toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.addActionListener(toolbarListener);

		return button;
	}

	/**
	 * Legt einen neuen Symbolleisten-Eintrag an
	 * @param toolbar	Übergeordnetes Symbolleisten-Element
	 * @param title	Name des neuen Symbolleisten-Eintrags
	 * @param hint	Zusätzlich anzuzeigender Tooltip für den Symbolleisten-Eintrag (kann <code>null</code> sein, wenn kein Tooltip angezeigt werden soll)
	 * @param icon	Pfad zu dem Icon, das in dem Symbolleisten-Eintrag angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @return	Neu erstellter Symbolleisten-Eintrag
	 */
	protected JButton createToolbarButton(final JToolBar toolbar, final String title, final String hint, final Icon icon) {
		JButton button=new JButton(title);
		if (toolbar!=null) toolbar.add(button);
		if (hint!=null) button.setToolTipText(hint);
		button.addActionListener(toolbarListener);
		if (icon!=null) button.setIcon(icon);
		return button;
	}

	/**
	 * Generiert basierend auf einem Hotkey die Textbeschreibung für den Hotkey (z.B. zur Anzeige in Symbolleisten-Schaltflächen Tooltips)
	 * @param key	Hotkey
	 * @return	Textbeschreibung für den Hotkey
	 */
	private String keyStrokeToString(final KeyStroke key) {
		final int modifiers=key.getModifiers();
		final StringBuilder text=new StringBuilder();
		if (modifiers>0) {
			text.append(InputEvent.getModifiersExText(modifiers));
			text.append('+');
		}
		text.append(KeyEvent.getKeyText(key.getKeyCode()));
		return text.toString();
	}

	/**
	 * Erzeugte die vertikale Symbolleiste links
	 * @return	Liefert {@link #leftArea} zurück
	 * @see #leftArea
	 */
	private JComponent createLeftToolBar() {
		final SetupData setup=SetupData.getSetup();

		leftArea=new JPanel(new BorderLayout());

		JToolBar leftToolbar=new JToolBar(SwingConstants.VERTICAL);
		leftArea.add(leftToolbar,BorderLayout.WEST);
		leftToolbar.setFloatable(false);

		final String hotkeyProperties=keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F2,InputEvent.CTRL_DOWN_MASK));
		buttonProperties=createRotatedToolbarButton(leftToolbar,Language.tr("Editor.ModelProperties.Short"),Language.tr("Editor.ModelProperties.Info")+" ("+hotkeyProperties+")",Images.MODEL.getIcon());
		if (!readOnly) {
			final String hotkeyToggleTemplates=keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
			buttonTemplates=createRotatedToolbarButton(leftToolbar,Language.tr("Editor.ToggleTemplates.Short"),Language.tr("Editor.ToggleTemplates.Info")+" ("+hotkeyToggleTemplates+")",Images.ELEMENTTEMPLATES.getIcon());
			buttonAddEdge=createRotatedToolbarButton(leftToolbar,Language.tr("Editor.AddEdge.Short"),Language.tr("Editor.AddEdge.Info"),Images.EDIT_EDGES_ADD.getIcon());
			buttonAddEdge.setEnabled(!readOnly);
		}

		if (!readOnly) {
			leftToolbar.add(separatorUndoRedo=new JToolBar.Separator(null));
			buttonUndo=createToolbarButton(leftToolbar,"",Language.tr("Main.Menu.Edit.Undo")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_Z,InputEvent.CTRL_DOWN_MASK))+")",Images.EDIT_UNDO.getIcon());
			buttonUndo.setEnabled(false);
			buttonRedo=createToolbarButton(leftToolbar,"",Language.tr("Main.Menu.Edit.Redo")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_Y,InputEvent.CTRL_DOWN_MASK))+")",Images.EDIT_REDO.getIcon());
			buttonRedo.setEnabled(false);
		}

		templates=new JList<>();
		templates.setCellRenderer(templatesRenderer=new ModelElementCatalogListCellRenderer<>());
		if (setup.onlyOneOpenTemplatesGroup) enforceOnlyOneGroupOpen();

		templates.setModel(ModelElementCatalog.getCatalog().getTemplatesListModel(setup.visibleTemplateGroups,setup.openTemplateGroups,"",model.surface.getParentSurface()!=null));

		templates.setDragEnabled(true);
		templates.setTransferHandler(new ModelElementCatalogTransferHandler());
		templates.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					final int index=templates.locationToIndex(e.getPoint());
					if (index<0) return;
					final ModelElementPosition element=templates.getModel().getElementAt(index);
					if (!(element instanceof ModelElementListGroup)) return;
					((ModelElementListGroup)element).toggleShowSub();
					saveGroupOpenStatus(((ModelElementListGroup)element).getIndex(),((ModelElementListGroup)element).isShowSub());
					updateTemplatesFilter();
					ListModel<ModelElementPosition> model=templates.getModel();
					templates.setModel(new DefaultListModel<>());
					templates.setModel(model);
					setTemplatesVisible(isTemplatesVisible());
				}
			}
		});
		final JScrollPane leftAreaScrollPane=new JScrollPane(templates);

		leftAreaTemplates=new JPanel(new BorderLayout());
		final JPanel sub=new JPanel(new BorderLayout());
		leftAreaTemplates.add(sub,BorderLayout.NORTH);
		sub.add(getTopInfoPanel(Language.tr("Editor.Templates"),Images.ELEMENTTEMPLATES.getIcon(),e->setTemplatesVisible(false),null,"F2"),BorderLayout.NORTH);

		sub.add(leftAreaQuickFilter=new JPlaceholderTextField(),BorderLayout.CENTER);
		leftAreaQuickFilter.setPlaceholder(Language.tr("Editor.QuickFilter"));
		leftAreaQuickFilter.setToolTipText(Language.tr("Editor.QuickFilter.Tooltip"));
		leftAreaQuickFilter.addKeyListener(new KeyListener() {
			@Override public void keyTyped(KeyEvent e) {updateTemplatesFilter(); leftAreaQuickFilter.requestFocus();}
			@Override public void keyReleased(KeyEvent e) {updateTemplatesFilter(); leftAreaQuickFilter.requestFocus();}
			@Override public void keyPressed(KeyEvent e) {updateTemplatesFilter(); leftAreaQuickFilter.requestFocus();}
		});
		final JToolBar miniToolbar=new JToolBar(SwingConstants.HORIZONTAL);
		miniToolbar.setFloatable(false);
		sub.add(miniToolbar,BorderLayout.EAST);
		miniToolbar.add(leftAreaTemplatesFilterButton=new JButton());
		leftAreaTemplatesFilterButton.setToolTipText(Language.tr("Editor.TemplateFilter.Tooltip"));
		leftAreaTemplatesFilterButton.setIcon(Images.ELEMENTTEMPLATES_FILTER.getIcon());
		leftAreaTemplates.add(leftAreaScrollPane,BorderLayout.CENTER);
		leftArea.add(leftAreaTemplates,BorderLayout.CENTER);
		leftAreaTemplatesFilterButton.addActionListener(e->showFilterTemplatesPopup());

		if (!readOnly) setTemplatesVisible(setup.startTemplateMode==SetupData.StartTemplateMode.START_TEMPLATE_VISIBLE || (setup.startTemplateMode==SetupData.StartTemplateMode.START_TEMPLATE_LASTSTATE && setup.showTemplates)); else setTemplatesVisible(false);

		leftToolbar.add(Box.createVerticalGlue());
		buttonExplorer=createRotatedToolbarButton(leftToolbar,Language.tr("Editor.ModelOverview.Short"),Language.tr("Editor.ModelOverview.Info")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_F12,InputEvent.CTRL_DOWN_MASK))+")",Images.GENERAL_FIND.getIcon());

		return leftArea;
	}

	/**
	 * Erzeugt das Navigator-Panel auf der rechten Seite
	 * @return	Liefert {@link #rightArea} zurück
	 * @see #rightArea
	 */
	private JComponent createNavigatorPanel() {
		navigator=new JList<>();
		navigator.setCellRenderer(navigatorRenderer=new ModelElementNavigatorListCellRenderer<>());
		navigator.setModel(navigatorModel=new DefaultListModel<>());

		navigator.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					final int index=navigator.locationToIndex(e.getPoint());
					if (index<0 || index>=navigator.getModel().getSize()) return;
					final ModelElementBox element=navigator.getModel().getElementAt(index);
					surfacePanel.selectAndScrollToElement(element.getId());
				}
			}
		});

		rightArea=new JPanel(new BorderLayout());
		rightArea.add(getTopInfoPanel(Language.tr("Editor.Navigator"),Images.NAVIGATOR.getIcon(),null,e->setNavigatorVisible(false),"F12"),BorderLayout.NORTH);

		rightArea.add(new JScrollPane(navigator),BorderLayout.CENTER);
		rightArea.setVisible(false);

		return rightArea;
	}

	/**
	 * Generiert eine Titelzeile oberhalb der seitlichen Panels (für Elementenvorlagen und Modellnavigator)
	 * @param title	Titel des seitlichen Panels
	 * @param icon	Icon zur Anzeige neben dem  seitlichen Panel (optional, kann <code>null</code> sein)
	 * @param actionLeft	Optionale Aktion zum Schließen (Icon links anzeigen) (kann <code>null</code> sein)
	 * @param actionRight	Optionale Aktion zum Schließen (Icon rechts anzeigen) (kann <code>null</code> sein)
	 * @param hotkey	Im Tooltip des Schließen-Icons anzuzeigender Hotkey
	 * @return	Neues Panel
	 */
	private JPanel getTopInfoPanel(final String title, final Icon icon, final ActionListener actionLeft, final ActionListener actionRight, final String hotkey) {
		final JPanel top=new JPanel(new BorderLayout());
		top.setBackground(Color.LIGHT_GRAY);

		final JLabel label=new JLabel(title);
		final Font font=label.getFont();
		label.setFont(new java.awt.Font(font.getFontName(),java.awt.Font.BOLD,font.getSize()+2));
		if (icon!=null) label.setIcon(icon);
		final JPanel middle=new JPanel(new FlowLayout(FlowLayout.CENTER));
		middle.setOpaque(false);
		middle.add(label);
		top.add(middle,BorderLayout.CENTER);

		if (actionLeft!=null) {
			final JToolBar toolbar=new JToolBar();
			toolbar.setFloatable(false);
			toolbar.setOpaque(false);
			top.add(toolbar,BorderLayout.WEST);
			final JButton button=new JButton();
			toolbar.add(button);
			button.setOpaque(false);
			button.addActionListener(actionLeft);
			button.setToolTipText(Language.tr("Editor.CloseSidebarTooltip")+((hotkey!=null && !hotkey.isEmpty())?(" ("+hotkey+")"):""));
			button.setIcon(Images.ELEMENTTEMPLATES_CLOSEPANEL.getIcon());
		} else {
			if (actionRight!=null) {
				final JPanel filler=new JPanel(new FlowLayout(FlowLayout.LEFT));
				filler.setOpaque(false);
				filler.add(Box.createHorizontalStrut(18));
				top.add(filler,BorderLayout.WEST);
			}
		}

		if (actionRight!=null) {
			final JToolBar toolbar=new JToolBar();
			toolbar.setFloatable(false);
			toolbar.setOpaque(false);
			top.add(toolbar,BorderLayout.EAST);
			final JButton button=new JButton();
			toolbar.add(button);
			button.setOpaque(false);
			button.addActionListener(actionRight);
			button.setToolTipText(Language.tr("Editor.CloseSidebarTooltip")+((hotkey!=null && !hotkey.isEmpty())?(" ("+hotkey+")"):""));
			button.setIcon(Images.NAVIGATOR_CLOSEPANEL.getIcon());
		} else {
			if (actionLeft!=null) {
				final JPanel filler=new JPanel(new FlowLayout(FlowLayout.LEFT));
				filler.setOpaque(false);
				filler.add(Box.createHorizontalStrut(18));
				top.add(filler,BorderLayout.EAST);
			}
		}

		return top;
	}

	/**
	 * Speichert die Einstellungen, welche Gruppen in der Elementvorlagenliste geöffnet sind im Setup.
	 * @param groupIndex	Index der Gruppe deren Status sich geändert hat
	 * @param open	Ist die Gruppe geöffnet?
	 * @see #templates
	 * @see #expandGroupForElement(ModelElementPosition)
	 */
	private void saveGroupOpenStatus(final int groupIndex, final boolean open) {
		final SetupData setup=SetupData.getSetup();
		final String s=setup.openTemplateGroups;
		final StringBuilder sb=new StringBuilder();

		int openCount=open?1:0;
		for (int i=0;i<ModelElementCatalog.getCatalog().getGroupCount();i++) {
			boolean visible=(i>=setup.visibleTemplateGroups.length() || (setup.visibleTemplateGroups.charAt(i)!='-' && setup.visibleTemplateGroups.charAt(i)!='0'));
			if (visible && setup.onlyOneOpenTemplatesGroup) {
				if (i==groupIndex) {
					sb.append(open?'X':'-');
				} else {
					if (s.length()<=i || s.charAt(i)=='X') {
						openCount++;
						if (openCount<2) sb.append('X'); else sb.append('-');
					} else {
						sb.append('-');
					}
				}
			} else {
				if (i==groupIndex) sb.append(open?'X':'-'); else sb.append((i>=s.length())?'X':s.charAt(i));
			}
		}

		setup.openTemplateGroups=sb.toString();
		setup.saveSetup();
	}

	/**
	 * Aktualisiert die Darstellung innerhalb der Elementenvorlagenliste
	 * (geöffnete und geschlossene Gruppen, Filtereingabe).
	 * @see #templates
	 */
	private void updateTemplatesFilter() {
		final SetupData setup=SetupData.getSetup();
		final String filter=leftAreaQuickFilter.getText().trim();
		final String visibleGroups=setup.visibleTemplateGroups;
		final String openGroups=setup.openTemplateGroups;
		templates.setModel(ModelElementCatalog.getCatalog().getTemplatesListModel(visibleGroups,openGroups,filter,getModel().surface.getParentSurface()!=null));
		setTemplatesVisible(isTemplatesVisible());
	}

	/**
	 * Listener, die benachrichtigt werden, wenn eine Datei auf der Komponente abgelegt wird
	 * @see #dropFile(FileDropperData)
	 */
	private final List<ActionListener> fileDropListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn eine Datei auf der Komponente abgelegt wird
	 * @param fileDropListener	Zu benachrichtigender Listener (der Dateiname ist über die <code>getActionCommand()</code>-Methode des übergebenen <code>ActionEvent</code>-Objekts abrufbar)
	 */
	public void addFileDropListener(final ActionListener fileDropListener) {
		if (fileDropListeners.indexOf(fileDropListener)<0) fileDropListeners.add(fileDropListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer auf dieser Komponente abgelegten Datei zu benachrichtigenden Listener
	 * @param fileDropListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeFileDropListener(final ActionListener fileDropListener) {
		return fileDropListeners.remove(fileDropListener);
	}

	/**
	 * Löst die Listener, die benachrichtigt werden, wenn eine Datei auf der Komponente abgelegt wird, aus.
	 * @param data	Informationen zu der abgelegten Datei
	 */
	private void dropFile(final FileDropperData data) {
		final ActionEvent event=FileDropperData.getActionEvent(data);
		for (ActionListener listener: fileDropListeners) listener.actionPerformed(event);
	}

	@Override
	protected void buildGUI() {
		toolbarListener=new ToolbarListener();
		setLayout(new BorderLayout());

		JPanel p=new JPanel(new BorderLayout());
		add(p,BorderLayout.NORTH);
		p.add(additionalInfoArea=new JToolBar(),BorderLayout.CENTER);
		additionalInfoArea.setVisible(false);
		additionalInfoArea.setFloatable(false);
		additionalInfoArea.setBackground(new Color(255,255,240));
		additionalInfoArea.add(additionalInfoLabel=new JLabel(""));
		additionalInfoLabel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		additionalInfoArea.add(Box.createHorizontalGlue());
		JButton button=new JButton(Language.tr("Editor.AddEdge.Hint.RemoveButton"));
		button.setBackground(new Color(255,255,240));
		button.setToolTipText(Language.tr("Editor.AddEdge.Hint.RemoveButton.Hint"));
		additionalInfoArea.add(button);
		button.setIcon(Images.EDIT_EDGES_ADD_CLOSEPANEL.getIcon());
		button.addActionListener(e->{
			additionalInfoArea.setVisible(false);
			InfoPanel.getInstance().setVisible(InfoPanel.globalAddEdge,false);
		});

		JPanel main=new JPanel(new BorderLayout());
		add(main,BorderLayout.CENTER);

		main.add(createLeftToolBar(),BorderLayout.WEST);

		final boolean isSubSurface=(model!=null && model.surface!=null && model.surface.getParentSurface()!=null);
		surfacePanel=new ModelSurfacePanel(readOnly,!isSubSurface);
		surfacePanel.setAdditionalTooltipGetter(box->getStatisticsInfoForElement(box));
		surfacePanel.addSelectionListener(e->fireSelectionListener());
		surfacePanel.addLinkListener(link->fireLinkListener(link));
		surfacePanel.setRaster(SetupData.getSetup().grid);
		if (model!=null) surfacePanel.setColors(model.surfaceColors);
		rulerPanel=new RulerPanel(surfacePanel,SetupData.getSetup().showRulers);
		surfacePanel.addStateChangeListener(e->updateStatusBar());
		surfacePanel.addUndoRedoDoneListener(e->updateCanUndoRedo());
		surfacePanel.setDropAccept(true);
		surfacePanel.addFileDropListener(e->dropFile((FileDropperData)e.getSource()));
		surfacePanel.addShowModelPropertiesListener(e->{
			final String cmd=e.getActionCommand();
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES)) {
				showModelPropertiesDialog(null);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_OPERATORS)) {
				showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.OPERATORS);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_PROPERTIES_TRANSPORTERS)) {
				showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.TRANSPORTERS);
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_BACKGROUND)) {
				showBackgroundColorDialog();
				return;
			}
			if (cmd.equals(ModelSurfacePanel.PROPERTIES_TYPE_LAYERS)) {
				showLayersDialog();
				return;
			}
		});
		surfacePanel.addResourceCountSetter((name,count)->changeResourceCount(name,count.intValue()));
		surfacePanel.addZoomChangeListener(e->{
			labelZoom.setText(Math.round(100*surfacePanel.getZoom())+"% ");
			templatesRenderer.setZoom(surfacePanel.getZoom());
			updateTemplatesFilter();
			navigatorRenderer.setZoom(surfacePanel.getZoom());
			navigatorModel.clear();
			updateNavigatorList();
		});
		surfacePanel.addBuildParameterSeriesListener(template->fireBuildParameterSeries(template));
		surfacePanel.addSetElementTemplatesVisibilityListener(visibility->{switch (visibility) {
		case HIDE: setTemplatesVisible(false); break;
		case SHOW: setTemplatesVisible(true); break;
		case TOGGLE: setTemplatesVisible(!isTemplatesVisible()); break;
		}});
		main.add(rulerPanel,BorderLayout.CENTER);

		main.add(createNavigatorPanel(),BorderLayout.EAST);

		JPanel statusPanel=new JPanel(new BorderLayout());
		add(statusPanel,BorderLayout.SOUTH);
		statusPanel.add(statusBar=new JLabel(""),BorderLayout.CENTER);
		statusBar.setBorder(BorderFactory.createEmptyBorder(0,5,0,0));
		JPanel zoomArea=new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
		statusPanel.add(zoomArea,BorderLayout.EAST);
		statusBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) && buttonProperties.isVisible()) {
					setupInfoLabels(true); repaint(); showModelPropertiesDialog(ModelPropertiesDialog.InitialPage.INFO);
				}
			}
		});

		zoomArea.add(labelZoom=new JLabel(Math.round(100*surfacePanel.getZoom())+"% "));
		labelZoom.addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {showZoomContextMenu(labelZoom);}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {}
		});
		labelZoom.setToolTipText(Language.tr("Editor.SetupZoom"));
		zoomArea.add(buttonZoomOut=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomOut")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,InputEvent.CTRL_DOWN_MASK))+")",Images.ZOOM_OUT.getIcon()));
		buttonZoomOut.setPreferredSize(new Dimension(20,20));
		buttonZoomOut.setBorderPainted(false);
		buttonZoomOut.setFocusPainted(false);
		buttonZoomOut.setContentAreaFilled(false);
		zoomArea.add(buttonZoomIn=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomIn")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,InputEvent.CTRL_DOWN_MASK))+")",Images.ZOOM_IN.getIcon()));
		buttonZoomIn.setPreferredSize(new Dimension(20,20));
		buttonZoomIn.setBorderPainted(false);
		buttonZoomIn.setFocusPainted(false);
		buttonZoomIn.setContentAreaFilled(false);
		zoomArea.add(buttonZoomDefault=createToolbarButton(null,"",Language.tr("Main.Menu.View.ZoomDefault")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,InputEvent.CTRL_DOWN_MASK))+")",Images.ZOOM.getIcon()));
		buttonZoomDefault.setPreferredSize(new Dimension(20,20));
		buttonZoomDefault.setBorderPainted(false);
		buttonZoomDefault.setFocusPainted(false);
		buttonZoomDefault.setContentAreaFilled(false);
		zoomArea.add(buttonFindModel=createToolbarButton(null,"",Language.tr("Main.Menu.View.CenterModel")+" ("+keyStrokeToString(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0,InputEvent.CTRL_DOWN_MASK))+")",Images.ZOOM_CENTER_MODEL.getIcon()));
		buttonFindModel.setPreferredSize(new Dimension(20,20));
		buttonFindModel.setBorderPainted(false);
		buttonFindModel.setFocusPainted(false);
		buttonFindModel.setContentAreaFilled(false);

		updateStatusBar();
	}

	/**
	 * Aktualisiert die Auflistung der Elemente des Modells
	 * im Navigator.
	 * @see #navigator
	 */
	private void updateNavigatorList() {
		if (surfacePanel==null) return;
		if (surfacePanel.isOperationRunning()) return;
		final ModelSurface surface=surfacePanel.getSurface();

		final List<ModelElementBox> boxElements=surface.getElements().stream()
				.filter(element->surface.isVisibleOnLayer(element))
				.filter(element->element instanceof ModelElementBox)
				.map(element->(ModelElementBox)element)
				.collect(Collectors.toList());
		if (navigatorModel.getSize()!=boxElements.size()) {
			navigatorModel.clear();
			for (ModelElementBox element: boxElements) navigatorModel.addElement(element);
		} else {
			for (int i=0;i<boxElements.size();i++) if (navigatorModel.getElementAt(i)!=boxElements.get(i)) navigatorModel.setElementAt(boxElements.get(i),i);
		}
		final int navigatorIndex=boxElements.indexOf(surface.getSelectedElement());
		if (navigatorIndex>=0) {
			navigator.setSelectedIndex(navigatorIndex);
			navigator.ensureIndexIsVisible(navigatorIndex);
		}

		navigator.repaint();
	}

	/**
	 * HTML-Kopf für Statuszeilen-Benachrichtigungen
	 * @see #updateStatusBar()
	 */
	private static final String statusHTMLStart="<html><body>";

	/**
	 * HTML-Span-Beginn in Grün für Statuszeilen-Benachrichtigungen
	 * @see #updateStatusBar()
	 */
	private static final String statusHTMLGreen="<span style=\"color: green;\">";

	/**
	 * HTML-Span-Beginn in Orange für Statuszeilen-Benachrichtigungen
	 * @see #updateStatusBar()
	 */
	private static final String statusHTMLOrange="<span style=\"color: orange;\">";

	/**
	 * HTML-Span-Beginn in Rot für Statuszeilen-Benachrichtigungen
	 * @see #updateStatusBar()
	 */
	private static final String statusHTMLRed="<span style=\"color: red;\">";

	/**
	 * HTML-Span-Ende für Statuszeilen-Benachrichtigungen
	 * @see #updateStatusBar()
	 */
	private static final String statusHTMLSpanEnd="</span>";

	/**
	 * HTML-Fuß für Statuszeilen-Benachrichtigungen
	 * @see #updateStatusBar()
	 */
	private static final String statusHTMLEnd="</body></html>";

	/**
	 * Muss aufgerufen werden, wenn die Statuszeile aktualisiert werden soll.<br>
	 * (Z.B. nach dem Neuladen des Setup.)
	 */
	public void updateStatusBar() {
		if (surfacePanel==null) return;
		if (surfacePanel.isOperationRunning()) return;
		final ModelSurface surface=surfacePanel.getSurface();
		if (surface==null) return;
		final SetupData setup=SetupData.getSetup();

		if (buttonAddEdge!=null) buttonAddEdge.setSelected(surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP1 || surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP2);

		updateNavigatorList();

		if (statusBar!=null) switch (surfacePanel.getMode()) {
		case MODE_NORMAL:
			setAdditionalInfoLabel(false,null);
			final int count=surface.getElementCount();
			final String elements=count+" "+((count==1)?Language.tr("Editor.Element.Singular"):Language.tr("Editor.Element.Plural"));

			final EditModel model=getModel();
			final BackgroundSystem backgroundSystem=BackgroundSystem.getBackgroundSystem(this);
			String check="";
			String tooltip=null;
			Icon icon=null;
			if (!readOnly && setup.autoSaveMode==SetupData.AutoSaveMode.AUTOSAVE_ALWAYS && getLastFile()!=null && isModelChanged()) {
				saveModel(getLastFile());
			}

			if (backgroundSystem.canCheck(model.surface)) {
				final String error=backgroundSystem.process(model,!readOnly);
				if (error==null) {
					check=", "+statusHTMLGreen+Language.tr("Editor.Check.Ok")+statusHTMLSpanEnd;
				} else {
					check=", "+statusHTMLRed+Language.tr("Editor.Check.Error")+statusHTMLSpanEnd;
					tooltip=statusHTMLStart+statusHTMLRed+"<b>"+error+"</b>"+statusHTMLSpanEnd+statusHTMLEnd;
					icon=Images.GENERAL_WARNING.getIcon();
				}
			}
			setStatusBarInfo(statusHTMLStart+elements+check+statusHTMLEnd,tooltip,icon);
			break;
		case MODE_ADD_ELEMENT:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceElement")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_STATION.getIcon());
			break;
		case MODE_ADD_EDGE_STEP1:
			setAdditionalInfoLabel(InfoPanel.getInstance().isVisible(InfoPanel.globalAddEdge),statusHTMLStart+"<b>"+Language.tr("Editor.AddEdge.PlacePoint1.Long")+"</b>"+statusHTMLEnd);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddEdge.PlacePoint1")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.EDIT_EDGES_ADD.getIcon());
			break;
		case MODE_ADD_EDGE_STEP2:
			setAdditionalInfoLabel(InfoPanel.getInstance().isVisible(InfoPanel.globalAddEdge),statusHTMLStart+"<b>"+Language.tr("Editor.AddEdge.PlacePoint2.Long")+"</b>"+statusHTMLEnd);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddEdge.PlacePoint2")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.EDIT_EDGES_ADD.getIcon());
			break;
		case MODE_INSERT_ELEMENTS_FROM_CLIPBOARD:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceClipboardElement")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_STATION.getIcon());
			break;
		case MODE_INSERT_ELEMENTS_FROM_TEMPLATE:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceTemplateElement")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_STATION.getIcon());
			break;
		case MODE_INSERT_IMAGE:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceClipboardImage")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_IMAGE.getIcon());
			break;
		case MODE_INSERT_TEXT:
			setAdditionalInfoLabel(false,null);
			setStatusBarInfo(statusHTMLStart+statusHTMLOrange+Language.tr("Editor.AddElement.PlaceClipboardText")+statusHTMLSpanEnd+statusHTMLEnd,null,Images.MODEL_ADD_TEXT.getIcon());
			break;
		}
	}

	/**
	 * Zeigt eine zusätzliche Infozeile über dem Modelleditor an.
	 * @param show	Zeile anzeigen oder ausblenden?
	 * @param text	Anzuzeigender Text
	 * @see #additionalInfoArea
	 * @see #additionalInfoLabel
	 */
	private void setAdditionalInfoLabel(final boolean show, final String text) {
		additionalInfoArea.setVisible(show && text!=null);
		if (text!=null) additionalInfoLabel.setText(statusHTMLStart+"<b>"+text+"</b>"+statusHTMLEnd);

		additionalInfoLabel.setIcon(Images.GENERAL_INFO.getIcon());
	}

	/**
	 * Stellt den Text für die Statuszeile ein.
	 * @param text	Anzuzeigender Text
	 * @param tooltip	Tooltip für den Text
	 * @param icon	Icon für den Text
	 * @see #updateStatusBar()
	 */
	private void setStatusBarInfo(final String text, final String tooltip, final Icon icon) {
		statusBar.setText(text);
		statusBar.setToolTipText(tooltip);
		statusBar.setIcon(icon);
	}

	/**
	 * Blendet die Statuszeile ein oder aus
	 * @param statusBarVisible	Sichtbarkeitsstatus der Statuszeile
	 */
	public void setStatusBarVisible(final boolean statusBarVisible) {
		statusBar.setVisible(statusBarVisible);
	}

	/**
	 * Schaltet einige Funktionen zum Bearbeiten auch während des Read-Only-Modus frei.<br>
	 * Erlaubt wird das Bearbeiten von Elementen sowie das Verschieben.
	 * Das Löschen von Elementen und das Erstellen neuer Elemente bleibt aber verboten.
	 */
	public void allowEditorDialogs() {
		allowChangeOperationsOnReadOnly=true;
		surfacePanel.allowEditorDialogs();
	}

	/**
	 * Aktualisiert die Schaltflächen {@link #buttonUndo} und {@link #buttonRedo}
	 * @see #buttonUndo
	 * @see #buttonRedo
	 */
	private void updateCanUndoRedo() {
		buttonUndo.setEnabled(!readOnly && surfacePanel.canUndo());
		buttonRedo.setEnabled(!readOnly && surfacePanel.canRedo());
		fireUndoRedoDoneListener();
	}

	@Override
	protected void writeGUIDataToModel() {
		model.name=name;
		model.description=description;
		model.author=author;
		model.clientCount=clientCount;
		model.warmUpTime=warmUpTime;
		ModelSurface surface=surfacePanel.getSurface();
		model.resources=surface.getResources().clone();
		model.schedules=surface.getSchedules().clone();
		model.surface=surface.clone(false,model.resources,model.schedules,surface.getParentSurface(),model);
		final Color[] colors=surfacePanel.getColors();
		model.surfaceColors[0]=colors[0];
		model.surfaceColors[1]=colors[1];
		if (colors.length>2) model.surfaceColors[2]=colors[2];
	}

	@Override
	protected void loadGUIDataFromModel() {
		name=model.name;
		description=model.description;
		author=model.author;
		clientCount=model.clientCount;
		warmUpTime=model.warmUpTime;
		if (surfacePanel!=null) {
			surfacePanel.setColors(model.surfaceColors); /* Reihenfolge ist wichtig. setSurface würde bedingt durch fireNotify Farbe von Modell aus surfacePanel überschreiben, daher erst Farbe aus Modell in surfacePanel übertragen. */
			surfacePanel.setSurface(model,model.surface.clone(false,model.resources.clone(),model.schedules.clone(),model.surface.getParentSurface(),model),model.clientData,model.sequences);
		}
		if (model.surface.getElementCount()>0) setupInfoLabels(true);
	}

	/**
	 * Zeigt den Modelleigenschaftendialog an.
	 * @param initialPage	Beim Aufruf des Dialogs anzuzeigende Seite (darf <code>null</code> sein)
	 */
	public void showModelPropertiesDialog(final ModelPropertiesDialog.InitialPage initialPage) {
		final ModelPropertiesDialog dialog=new ModelPropertiesDialog(owner,getModel(),readOnly,initialPage);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK && !getModel().equalsEditModel(dialog.getModel())) {
			File file=getLastFile();
			setModel(dialog.getModel());
			setModelChanged(true);
			setLastFile(file);
		}
		final ModelPropertiesDialog.NextAction nextAction=dialog.getNextAction();
		if (nextAction!=null) fireNextAction(nextAction);
	}

	/**
	 * Listener, die benachrichtigt werden, wenn der Modelleigenschaften-Dialog geschlossen wird und eine weitere Aktion ausgeführt werden soll
	 * @see #fireNextAction(ui.modelproperties.ModelPropertiesDialog.NextAction)
	 */
	private final List<Consumer<ModelPropertiesDialog.NextAction>> nextActionListeners=new ArrayList<>();

	/**
	 * Löst die Listener, die benachrichtigt werden, wenn der Modelleigenschaften-Dialog geschlossen wird und eine weitere Aktion ausgeführt werden soll, aus.
	 * @param nextAction	Auszuführende nächste Aktion
	 * @see #nextActionListeners
	 */
	private void fireNextAction(final ModelPropertiesDialog.NextAction nextAction) {
		nextActionListeners.forEach(action->action.accept(nextAction));
	}

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn der Modelleigenschaften-Dialog geschlossen wird und eine weitere Aktion ausführen will.
	 * @param listener	Zu benachrichtigender Listener
	 * @return	Liefert <code>true</code>, wenn der Listener zu der Liste hinzugefügt werden konnte.
	 */
	public boolean addNextActionListener(final Consumer<ModelPropertiesDialog.NextAction> listener) {
		return nextActionListeners.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die benachrichtigt werden sollen, wenn der Modelleigenschaften-Dialog geschlossen wird und eine weitere Aktion ausführen will.
	 * @param listener	Nicht mehr zu benachrichtigender Listener
	 * @return	Liefert <code>true</code>, wenn der Listener aus der Liste entfernt werden konnte.
	 */
	public boolean removeNextActionListener(final Consumer<ModelPropertiesDialog.NextAction> listener) {
		return nextActionListeners.remove(listener);
	}

	/**
	 * Zeigt den Dialog zum Bearbeiten der Ebenen an.
	 */
	public void showLayersDialog() {
		final LayersDialog dialog=new LayersDialog(owner,getModel(),readOnly);
		dialog.setVisible(true);
		if (dialog.getClosedBy()==BaseDialog.CLOSED_BY_OK && !getModel().equalsEditModel(dialog.getModel())) {
			File file=getLastFile();
			setModel(dialog.getModel());
			setModelChanged(true);
			setLastFile(file);
		}
	}

	/**
	 * Zeigt den Dialog zur Konfiguration von Hintergrund- und Rasterfarbe an.
	 */
	public void showBackgroundColorDialog() {
		final EditModel model=getModel();

		final BackgroundColorDialog dialog=new BackgroundColorDialog(owner,model.surfaceColors,readOnly);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		final Color[] colors=dialog.getColors();
		if (colors==null || colors.length!=3) return;
		if (!Objects.deepEquals(model.surfaceColors,colors)) {
			model.surfaceColors[0]=colors[0];
			model.surfaceColors[1]=colors[1];
			model.surfaceColors[2]=colors[2];
			final File file=getLastFile();
			setModel(model);
			setLastFile(file);
			setModelChanged(true);
		}
	}

	/**
	 * Wird von {@link ModelSurfacePanel} aufgerufen, um mitzuteilen,
	 * dass sich die Anzahl an Bedienern in einer Gruppe verändern soll
	 * (ausgelöst dort über das Kontextmenü).
	 * @param name	Name der Bedienergruppe
	 * @param count	Neue Anzahl
	 * @return	Liefert <code>true</code>, wenn die angegebene Gruppe existiert und die Änderung möglich war (weil die Gruppe über eine Anzahl definiert ist)
	 * @see ModelSurfacePanel#addResourceCountSetter
	 */
	private boolean changeResourceCount(final String name, final int count) {
		final EditModel model=getModel();
		final ModelResource resource=model.resources.getNoAutoAdd(name);
		if (resource==null || resource.getMode()!=ModelResource.Mode.MODE_NUMBER) return false;
		if (count<=0) return false;
		if (resource.getCount()==count) return true;
		resource.setCount(count);
		setModel(model);
		setModelChanged(true);
		return true;
	}

	/**
	 * Listener für Klicks auf die verschiedenen Symbolleisten-Schaltflächen
	 * @see EditorPanel#toolbarListener
	 */
	private class ToolbarListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			final Object source=e.getSource();

			if (source==buttonProperties) {setupInfoLabels(true); repaint(); showModelPropertiesDialog(null); return;}
			if (source==buttonTemplates) {setTemplatesVisible(!isTemplatesVisible()); return;}
			if (source==buttonAddEdge) {
				setupInfoLabels(true);
				if (surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP1 || surfacePanel.getMode()==ModelSurfacePanel.ClickMode.MODE_ADD_EDGE_STEP2) {
					surfacePanel.cancelAdd();
					return;
				}
				surfacePanel.startAddEdge();
				return;
			}
			if (source==buttonZoomOut) {zoomOut();  return;}
			if (source==buttonZoomIn) {zoomIn(); return;}
			if (source==buttonZoomDefault) {zoomDefault(); return;}
			if (source==buttonFindModel) {centerModel(); return;}
			if (source==buttonUndo) {doUndo(); return;}
			if (source==buttonRedo) {doRedo(); return;}
			if (source==buttonExplorer) {showExplorer(buttonExplorer); return;}
		}
	}

	/**
	 * Zeigt den Modell-Exportieren-Dialog an.
	 * @param parent	Übergeordnetes Element
	 * @param title	Titel des Dateiauswahldialogs
	 * @return	Liefert im Erfolgsfall die gewählte Datei, sonst <code>null</code>
	 */
	private File showExportDialog(Component parent, final String title) {
		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(title);
		final FileFilter jpg=new FileNameExtensionFilter(Language.tr("FileType.jpeg")+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(Language.tr("FileType.gif")+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(Language.tr("FileType.png")+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(Language.tr("FileType.bmp")+" (*.bmp)","bmp");
		final FileFilter pdf=new FileNameExtensionFilter(Language.tr("FileType.PDF")+" (*.pdf)","pdf");
		final FileFilter svg=new FileNameExtensionFilter(Language.tr("FileType.svg")+" (*.svg)","svg");
		final FileFilter eps=new FileNameExtensionFilter(Language.tr("FileType.eps")+" (*.eps)","eps");
		final FileFilter docx=new FileNameExtensionFilter(Language.tr("FileType.WordImage")+" (*.docx)","docx");
		final FileFilter pptx=new FileNameExtensionFilter(Language.tr("SlidesGenerator.FileTypePPTX")+" (*.pptx)","pptx");
		final FileFilter html=new FileNameExtensionFilter(Language.tr("FileType.HTML")+" (*.html)","html");
		final FileFilter drawio=new FileNameExtensionFilter(Language.tr("FileType.drawio")+" (*.drawio)","drawio");
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		fc.addChoosableFileFilter(pdf);
		fc.addChoosableFileFilter(svg);
		fc.addChoosableFileFilter(eps);
		fc.addChoosableFileFilter(docx);
		fc.addChoosableFileFilter(pptx);
		fc.addChoosableFileFilter(html);
		fc.addChoosableFileFilter(drawio);
		fc.setFileFilter(png);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(parent)!=JFileChooser.APPROVE_OPTION) return null;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==pdf) file=new File(file.getAbsoluteFile()+".pdf");
			if (fc.getFileFilter()==svg) file=new File(file.getAbsoluteFile()+".svg");
			if (fc.getFileFilter()==eps) file=new File(file.getAbsoluteFile()+".eps");
			if (fc.getFileFilter()==docx) file=new File(file.getAbsoluteFile()+".docx");
			if (fc.getFileFilter()==pptx) file=new File(file.getAbsoluteFile()+".pptx");
			if (fc.getFileFilter()==html) file=new File(file.getAbsoluteFile()+".html");
			if (fc.getFileFilter()==drawio) file=new File(file.getAbsoluteFile()+".drawio");
		}

		return file;
	}

	/**
	 * Exportiert das aktuelle Modell als Bilddatei oder pdf. Wird <code>null</code> als Parameter angegeben, so wird zunächst ein Dateiauswahldialog angezeigt.
	 * @param file	Name der Datei, in der das Modell gespeichert werden soll. Wird <code>null</code> übergeben, so wird ein Dialog zur Auswahl der Datei angezeigt.
	 * @param force	Speichert die Datei in jedem Fall und überschreibt ggf. existierende Dateien ohne Rückfrage
	 * @return	Gibt im Erfolgsfall <code>null</code> zurück, sonst eine Fehlermeldung.
	 */
	public String exportModelToFile(File file, final boolean force) {
		if (file==null) {
			file=showExportDialog(getParent(),Language.tr("Editor.ExportModel"));
			if (file==null) return null;
		}

		if (file.exists() && !force) {
			if (!MsgBox.confirmOverwrite(getOwnerWindow(),file)) return null;
		}

		String format="png";
		if (file.getName().toLowerCase().endsWith(".jpg")) format="jpg";
		if (file.getName().toLowerCase().endsWith(".jpeg")) format="jpg";
		if (file.getName().toLowerCase().endsWith(".gif")) format="gif";
		if (file.getName().toLowerCase().endsWith(".bmp")) format="bmp";
		if (file.getName().toLowerCase().endsWith(".pdf")) format="pdf";
		if (file.getName().toLowerCase().endsWith(".svg")) format="svg";
		if (file.getName().toLowerCase().endsWith(".eps")) format="eps";
		if (file.getName().toLowerCase().endsWith(".docx")) format="docx";
		if (file.getName().toLowerCase().endsWith(".pptx")) format="pptx";
		if (file.getName().toLowerCase().endsWith(".html")) format="html";
		if (file.getName().toLowerCase().endsWith(".drawio")) format="drawio";

		if (format.equalsIgnoreCase("html")) {
			/* HTML-Modus */
			final HTMLOutputBuilder builder=new HTMLOutputBuilder(getModel());
			return builder.build(file);
		}

		if (format.equalsIgnoreCase("pptx")) {
			/* pptx-Modus */
			final SlidesGenerator slides=new SlidesGenerator(getModel(),getExportImage());
			if (!slides.save(file)) return Language.tr("Editor.ExportModel.Error");
			return null;
		}

		if (format.equalsIgnoreCase("drawio")) {
			/* draw.io-Modus */
			final DrawIOExport export=new DrawIOExport(file);
			final EditModel model=getModel();
			export.process(model.surface,model);
			if (!export.save()) return Language.tr("Editor.ExportModel.Error");
			return null;
		}

		final SetupData setup=SetupData.getSetup();

		if (format.equalsIgnoreCase("docx")) {
			/* docx-Modus */
			if (!surfacePanel.saveDOCXToFile(file,setup.imageSize,setup.imageSize,true)) return Language.tr("Editor.ExportModel.Error");
			return null;
		}

		/* Bild */
		if (!surfacePanel.saveImageToFile(file,format,setup.imageSize,setup.imageSize)) return Language.tr("Editor.ExportModel.Error");
		return null;
	}

	/**
	 * Kopiert das aktuelle Modell als Bild in die Zwischenablage.
	 */
	public void exportModelToClipboard() {
		ImageTools.copyImageToClipboard(getExportImage());
	}

	/**
	 * Liefert das Bild in der Form, in der es auch per {@link #exportModelToClipboard()}
	 * in die Zwischenablage kopiert werden würde, direkt zurück.
	 * @return	Bild des Modells
	 */
	public BufferedImage getExportImage() {
		final SetupData setup=SetupData.getSetup();
		final BufferedImage image=surfacePanel.getImage(setup.imageSize,setup.imageSize);
		final Graphics g=image.getGraphics();
		g.setClip(0,0,setup.imageSize,setup.imageSize);
		return image;
	}

	/**
	 * Kopiert die ausgewählten Elemente in die Zwischenablage.
	 */
	public void copySelectedElementsToClipboard() {
		surfacePanel.copyToClipboard();
	}

	/**
	 * Kopiert die ausgewählten Elemente in die Zwischenablage und löscht sie dann von der Zeichenfläche
	 */
	public void cutSelectedElementsToClipboard() {
		surfacePanel.copyToClipboard();
		surfacePanel.deleteSelectedElements();
	}

	/**
	 * Fügt die in der Zwischenablage befindlichen Elemente in die Zeichnung ein.
	 * Befinden sich keine Elemente in der Zwischenablage, dafür aber Text, so wird dieser
	 * als neues Text-Element eingefügt.
	 */
	public void pasteFromClipboard() {
		surfacePanel.pasteFromClipboard();
		setupInfoLabels(true);
	}

	/**
	 * Fügt die als Stream vorliegenden Elemente in die Zeichnung ein.
	 * @param data	Einzufügende Elemente
	 */
	public void pasteDirect(final ByteArrayInputStream data) {
		surfacePanel.pasteDirect(data);
		setupInfoLabels(true);
	}

	/**
	 * Löscht die gewählten Elemente.
	 */
	public void deleteSelectedElements() {
		surfacePanel.deleteSelectedElements();
	}

	/**
	 * Wählt alle Element aus.
	 */
	public void selectAll() {
		surfacePanel.selectAll();
	}

	/**
	 * Scrollt zu einem bestimmten Element und wählt es aus
	 * @param id	ID des auszuwählenden Elements
	 */
	public void selectAndScrollToElement(final int id) {
		surfacePanel.selectAndScrollToElement(id);
	}

	/**
	 * Gibt an, ob Rückgängig-Schritte vorhanden sind
	 * @return	Gibt <code>true</code> zurück, wenn die Rückgängig-Funktion zur Verfügung steht
	 * @see #doUndo()
	 */
	public boolean canUndo() {
		return surfacePanel.canUndo();
	}

	/**
	 * Gibt an, ob Wiederholen-Schritte vorhanden sind
	 * @return	Gibt <code>true</code> zurück, wenn die Wiederholen-Funktion zur Verfügung steht
	 * @see #doRedo()
	 */
	public boolean canRedo() {
		return surfacePanel.canRedo();
	}

	/**
	 * Führt (wenn möglich) einen Rückgängig-Schritt aus
	 * @see #canUndo()
	 */
	public void doUndo() {
		surfacePanel.doUndo();
	}

	/**
	 * Führt (wenn möglich) einen Wiederholen-Schritt aus
	 * @see #canUndo()
	 */
	public void doRedo() {
		surfacePanel.doRedo();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl des Rückgängig- oder Wiederholen-Schritts an.
	 * @see #canUndo()
	 * @see #canRedo()
	 */
	public void doUnDoRedoByDialog() {
		surfacePanel.doUnDoRedoByDialog();
	}

	/**
	 * Listener, die benachrichtigt werden, wenn sich die Verfügbarkeit von Undo/Redo-Schritten ändert
	 * @see #fireUndoRedoDoneListener()
	 */
	private final List<ActionListener> undoRedoDoneListener=new ArrayList<>();

	/**
	 * Löst die Listener, die benachrichtigt werden, wenn sich die Verfügbarkeit von Undo/Redo-Schritten ändert, aus.
	 * @see #undoRedoDoneListener
	 */
	private void fireUndoRedoDoneListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"undoredodone");
		for (ActionListener listener: undoRedoDoneListener) listener.actionPerformed(event);
	}

	/**
	 * Fügt einen Listener, der benachrichtigt wird, wenn sich die Verfügbarkeit von Undo/Redo-Schritten ändert, zu der Liste aller Listener hinzu.
	 * @param listener	Hinzuzufügender Listener
	 */
	public void addUndoRedoDoneListener(final ActionListener listener) {
		if (undoRedoDoneListener.indexOf(listener)<0) undoRedoDoneListener.add(listener);
	}

	/**
	 * Entfernt einen Listener, der benachrichtigt wird, wenn sich die Verfügbarkeit von Undo/Redo-Schritten ändert, aus der Liste aller Listener.
	 * @param listener	Zu entfernender Listener
	 * @return	Gibt <code>true</code> zurück, wenn sich der Listener bisher in der Liste befand
	 */
	public boolean removeUndoRedoDoneListener(final ActionListener listener) {
		return undoRedoDoneListener.remove(listener);
	}

	/**
	 * Liefert das Modell als Bild in einer drucktauglichen Variante
	 * @param size	Größe des Bildes (in x- bzw., y-Pixeln)
	 * @return	Modell als Bild
	 */
	public BufferedImage getPrintImage(final int size) {
		return surfacePanel.getImage(size,size);
	}

	/**
	 * Zeigt das Kontextmenü zur Auswahl des Zoomfaktors an.
	 * @param parent	Übergeordnetes Element zur Ausrichtung des Popupmenüs.
	 * @see #labelZoom
	 */
	private void showZoomContextMenu(final Component parent) {
		final JPopupMenu popup=new JPopupMenu();

		final int value=Math.max(1,Math.min(20,(int)Math.round(getZoom()*5)));

		final JSlider slider=new JSlider(SwingConstants.VERTICAL,1,20,value);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		final Dictionary<Integer,JComponent> labels=new Hashtable<>();
		labels.put(1,new JLabel("20%"));
		labels.put(2,new JLabel("40%"));
		labels.put(3,new JLabel("60%"));
		labels.put(4,new JLabel("80%"));
		labels.put(5,new JLabel("100%"));
		labels.put(6,new JLabel("120%"));
		labels.put(10,new JLabel("200%"));
		labels.put(15,new JLabel("300%"));
		labels.put(20,new JLabel("400%"));
		slider.setLabelTable(labels);
		slider.setPaintLabels(true);
		slider.setValue(value);
		slider.addChangeListener(e->setZoom(slider.getValue()/5.0));
		slider.setPreferredSize(new Dimension(slider.getPreferredSize().width,350));

		popup.add(slider);

		popup.show(parent,0,-350);
	}

	/**
	 * Stellt die Callback-Methode, die aufgerufen werden soll, wenn in der Modell-Übersicht auf "Suchen" geklickt wird, ein.
	 * @param callbackElementSearch	Callback-Methode, die aufgerufen werden soll, wenn in der Modell-Übersicht auf "Suchen" geklickt wird
	 */
	public void setElementSearchCallback(final Runnable callbackElementSearch) {
		this.callbackElementSearch=callbackElementSearch;
	}

	/**
	 * Stellt die Callback-Methode, die aufgerufen werden soll, wenn in der Modell-Übersicht auf "Elementeliste" geklickt wird, ein.
	 * @param callbackElementList	Callback-Methode, die aufgerufen werden soll, wenn in der Modell-Übersicht auf "Elementeliste" geklickt wird
	 */
	public void setElementListCallback(final Runnable callbackElementList) {
		this.callbackElementList=callbackElementList;
	}

	/**
	 * Zeigt die Modellübersicht an.
	 */
	public void showExplorer() {
		showExplorer(buttonExplorer);
	}

	/**
	 * Zeigt den Modellüberblick (Modell-Explorer) an.
	 * @param parent	Übergeordnetes Element zur Ausrichtung des Modellüberblicks-Panels
	 * @see #buttonExplorer
	 */
	private void showExplorer(final Component parent) {
		final JPopupMenu popup=new JPopupMenu();

		final JPanel explorerFrame=new JPanel(new BorderLayout());

		final ModelExplorer explorer=new ModelExplorer(surfacePanel,500,250);
		explorerFrame.add(explorer,BorderLayout.CENTER);

		final JToolBar toolbar=new JToolBar();
		toolbar.setOrientation(SwingConstants.HORIZONTAL);
		toolbar.setFloatable(false);
		JButton button;
		if (!isNavigatorVisible()) {
			toolbar.add(button=new JButton(Language.tr("Editor.ModelOverview.Navigator")));
			button.setToolTipText(Language.tr("Editor.ModelOverview.Navigator.Hint"));
			final JButton showNavigator=button;
			button.addActionListener(e->{setNavigatorVisible(true); showNavigator.setVisible(false);});
			button.setIcon(Images.NAVIGATOR.getIcon());
		}
		toolbar.add(button=new JButton(Language.tr("Editor.ModelOverview.Search")));
		button.setToolTipText(Language.tr("Editor.ModelOverview.Search.Hint"));
		button.addActionListener(e->{if (callbackElementSearch!=null) callbackElementSearch.run();});
		button.setIcon(Images.GENERAL_FIND.getIcon());
		toolbar.add(button=new JButton(Language.tr("Editor.ModelOverview.List")));
		button.setToolTipText(Language.tr("Editor.ModelOverview.List.Hint"));
		button.addActionListener(e->{if (callbackElementList!=null) callbackElementList.run();});
		button.setIcon(Images.MODEL_LIST_ELEMENTS.getIcon());
		explorerFrame.add(toolbar,BorderLayout.SOUTH);

		popup.add(explorerFrame);
		popup.show(parent,parent.getWidth()+1,-explorer.getHeight()-30+parent.getHeight());
	}

	/**
	 * Stellt sicher, dass nur eine Gruppe in der Elementenvorlagenliste
	 * gleichzeitig geöffnet ist.
	 * @see #showFilterTemplatesPopup()
	 * @see #templates
	 */
	private void enforceOnlyOneGroupOpen() {
		final SetupData setup=SetupData.getSetup();

		/* Anzahl an offenen Gruppen bestimmen */
		int openCount=0;
		for (int i=0;i<ModelElementCatalog.getCatalog().getGroupCount();i++) {
			final boolean visible=setup.visibleTemplateGroups.length()<=i || (setup.visibleTemplateGroups.charAt(i)!='-' && setup.visibleTemplateGroups.charAt(i)!='0');
			if (visible && (setup.openTemplateGroups.length()<=i || setup.openTemplateGroups.charAt(i)=='X')) openCount++;
		}
		if (openCount<=1) return;

		final StringBuilder sb=new StringBuilder();
		openCount=0;
		for (int i=0;i<ModelElementCatalog.getCatalog().getGroupCount();i++) {
			final boolean visible=setup.visibleTemplateGroups.length()<=i || (setup.visibleTemplateGroups.charAt(i)!='-' && setup.visibleTemplateGroups.charAt(i)!='0');
			boolean open=(setup.openTemplateGroups.length()<=i || setup.openTemplateGroups.charAt(i)=='X');
			if (visible) {
				if (open) openCount++;
				if (openCount>1) sb.append('-'); else sb.append(open?'X':'-');
			} else {
				sb.append(open?'X':'-');
			}
		}
		setup.openTemplateGroups=sb.toString();
		setup.saveSetup();
	}

	/**
	 * Zeigt das Popupmenü zur Filterung der Vorlagen
	 * in der Vorlagen-Leiste an.
	 * @see #leftAreaTemplatesFilterButton
	 */
	private void showFilterTemplatesPopup() {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Editor.TemplateFilter.Info")));
		item.setEnabled(false);
		popup.addSeparator();

		final SetupData setup=SetupData.getSetup();
		final String groups=setup.visibleTemplateGroups;
		for (int i=0;i<ModelElementCatalog.GROUP_ORDER.length;i++) {
			final JCheckBoxMenuItem check=new JCheckBoxMenuItem(ModelElementCatalog.GROUP_ORDER[i]);
			check.setSelected(groups.length()<=i || (groups.charAt(i)!='0' && groups.charAt(i)!='-'));
			popup.add(check);
			final int index=i;
			check.addActionListener(e->{
				String s=setup.visibleTemplateGroups;
				final StringBuilder sb=new StringBuilder();
				int j=Math.min(index,s.length());
				if (j>=1) sb.append(s.substring(0,j));
				while (sb.length()<index) sb.append("X");
				if (s.length()<=index || (s.charAt(index)!='0' && s.charAt(index)!='-')) sb.append('-'); else sb.append('X');
				if (s.length()>index+1) sb.append(s.substring(index+1));
				setup.visibleTemplateGroups=sb.toString();
				setup.saveSetup();
				if (setup.onlyOneOpenTemplatesGroup) enforceOnlyOneGroupOpen();
				updateTemplatesFilter();
			});
		}
		popup.addSeparator();
		item=new JMenuItem(Language.tr("Editor.TemplateFilter.ShowAll"));
		popup.add(item);
		item.addActionListener(e->{
			setup.visibleTemplateGroups="";
			if (setup.onlyOneOpenTemplatesGroup) enforceOnlyOneGroupOpen();
			setup.saveSetup();
			updateTemplatesFilter();
		});

		popup.addSeparator();

		item=new JMenuItem(Language.tr("Editor.TemplateFilter.OpenAll"));
		popup.add(item);
		item.setIcon(Images.ELEMENTTEMPLATES_GROUP_OPEN.getIcon());
		item.addActionListener(e->{
			setup.openTemplateGroups="";
			setup.onlyOneOpenTemplatesGroup=false;
			setup.saveSetup();
			updateTemplatesFilter();
		});
		item=new JMenuItem(Language.tr("Editor.TemplateFilter.CloseAll"));
		popup.add(item);
		item.setIcon(Images.ELEMENTTEMPLATES_GROUP_CLOSE.getIcon());
		item.addActionListener(e->{
			final StringBuilder sb=new StringBuilder();
			for (int i=0;i<ModelElementCatalog.getCatalog().getGroupCount();i++) sb.append("-");
			setup.openTemplateGroups=sb.toString();
			setup.saveSetup();
			updateTemplatesFilter();
		});
		final JCheckBoxMenuItem check=new JCheckBoxMenuItem(Language.tr("Editor.TemplateFilter.OnlyOneGroup"));
		check.setSelected(setup.onlyOneOpenTemplatesGroup);
		popup.add(check);
		check.addActionListener(e->{
			setup.onlyOneOpenTemplatesGroup=!setup.onlyOneOpenTemplatesGroup;
			setup.saveSetup();
			if (setup.onlyOneOpenTemplatesGroup) enforceOnlyOneGroupOpen();
			updateTemplatesFilter();
		});

		popup.show(leftAreaTemplatesFilterButton,0,leftAreaTemplatesFilterButton.getHeight());
	}

	/**
	 * Bewegt das momentan ausgewählte Element um eine oder mehrere Stufen in der Zeichenflächen-Hierarchie nach vorne
	 * @param max	Wird hier <code>true</code> übergeben, so wird das Element ganz nach vorne verschoben; ansonsten nur um eine Stufe nach vorne
	 */
	public void moveSelectedElementToFront(final boolean max) {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surfacePanel.moveSelectedElementToFront(max);
	}

	/**
	 * Bewegt das momentan ausgewählte Element um eine oder mehrere Stufen in der Zeichenflächen-Hierarchie nach hinten
	 * @param max	Wird hier <code>true</code> übergeben, so wird das Element ganz nach hinten verschoben; ansonsten nur um eine Stufe nach hinten
	 */
	public void moveSelectedElementToBack(final boolean max) {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surfacePanel.moveSelectedElementToBack(max);
	}

	/**
	 * Richtet die ausgewählten Element an einer gemeinsamen Oberkante aus.
	 */
	public void alignSelectedElementsTop() {
		surfacePanel.alignSelectedElementsTop();
	}

	/**
	 * Richtet die ausgewählten Element so aus, dass sich ihre vertikale Mitte auf derselben Höhe befindet.
	 */
	public void alignSelectedElementsMiddle() {
		surfacePanel.alignSelectedElementsMiddle();
	}

	/**
	 * Richtet die ausgewählten Element an einer gemeinsamen Unterkante aus.
	 */
	public void alignSelectedElementsBottom() {
		surfacePanel.alignSelectedElementsBottom();
	}

	/**
	 * Richtet die ausgewählten Element an einer gemeinsamen linken Kante aus.
	 */
	public void alignSelectedElementsLeft() {
		surfacePanel.alignSelectedElementsLeft();
	}

	/**
	 * Richtet die ausgewählten Element so aus, dass sich ihre horizontale Mitte auf derselben Linie befindet.
	 */
	public void alignSelectedElementsCenter() {
		surfacePanel.alignSelectedElementsCenter();
	}

	/**
	 * Richtet die ausgewählten Element an einer gemeinsamen rechten Kante aus.
	 */
	public void alignSelectedElementsRight() {
		surfacePanel.alignSelectedElementsRight();
	}

	/**
	 * Gibt an, ob Kanten zu neu eingefügten Elementen wenn möglich automatisch hinzugefügt werden sollen.
	 * @return	Kanten-Hinzufüge-Modus
	 */
	public ModelSurfacePanel.ConnectMode getAutoConnect() {
		return surfacePanel.getAutoConnect();
	}

	/**
	 * Stellt ein, ob Kanten zu neu eingefügten Elementen wenn möglich automatisch hinzugefügt werden sollen.
	 * @param autoConnect	Kanten-Hinzufüge-Modus
	 */
	public void setAutoConnect(final ModelSurfacePanel.ConnectMode autoConnect) {
		surfacePanel.setAutoConnect(autoConnect);
	}

	/**
	 * Listener, die benachrichtigt werden, wenn Elemente ausgewählt werden
	 * @see #fireSelectionListener()
	 */
	private final List<ActionListener> selectionListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn Elemente ausgewählt werden
	 * @param selectionListener	Zu benachrichtigender Listener
	 */
	public void addSelectionListener(final ActionListener selectionListener) {
		if (selectionListeners.indexOf(selectionListener)<0) selectionListeners.add(selectionListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer Auswahl von Elementen zu benachrichtigenden Listener
	 * @param selectionListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeSelectionListener(final ActionListener selectionListener) {
		return selectionListeners.remove(selectionListener);
	}

	/**
	 * Löst die Listener, die benachrichtigt werden, wenn Elemente ausgewählt werden, aus.
	 * @see #selectionListeners
	 */
	private void fireSelectionListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"selectionchanged");
		for (ActionListener listener: selectionListeners) listener.actionPerformed(event);
	}

	/**
	 * Listener, die benachrichtigt werden, wenn ein Zeichenflächen-Link angeklickt wird
	 * @see #fireLinkListener(int)
	 */
	private final List<IntConsumer> linkListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn ein Zeichenflächen-Link angeklickt wird
	 * @param linkListener	Zu benachrichtigender Listener
	 */

	public void addLinkListener(final IntConsumer linkListener) {
		if (linkListeners.indexOf(linkListener)<0) linkListeners.add(linkListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle eins Klicks auf einen Zeichenflächen-Link zu benachrichtigenden Listener
	 * @param linkListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeLinkListener(final IntConsumer linkListener) {
		return linkListeners.remove(linkListener);
	}

	/**
	 * Löst die Listener, die benachrichtigt werden, wenn ein Zeichenflächen-Link angeklickt wird, aus.
	 * @param link	Nummer des angeklickten Zeichenflächen-Links
	 * @see #linkListeners
	 */
	private void fireLinkListener(final int link) {
		for (IntConsumer linkListener: linkListeners) linkListener.accept(link);
	}

	/**
	 * Liefert einen direkten Verweis auf die in dem Editor verwendete Zeichenfläche.<br>
	 * Es können hier direkt die Elemente inkl. ihrem Selektionsstatus ausgelesen werden. Aber
	 * es dürfen nicht einfach extern Veränderungen vorgenommen werden.
	 * @return	Innerhalb des Editors verwendete Zeichenfläche
	 */
	public ModelSurface getOriginalSurface() {
		return surfacePanel.getSurface();
	}

	/**
	 * Stellt ein, ob die Lineale sichtbar sein sollen.
	 * @param visible	Lineale sichtbar
	 */
	public void setRulersVisible(final boolean visible) {
		rulerPanel.setRulerVisible(visible);
	}

	/**
	 * Liefert das aktuelle selektierte Element
	 * @return	Aktuell selektiertes Element oder <code>null</code>, falls nichts selektiert ist.
	 */
	public ModelElement getSelectedElement() {
		return surfacePanel.getSelectedElement();
	}

	/**
	 * Liefert eine Liste der Elemente, die über eine Bereichsselektion markiert sind.
	 * @param addRegularSelectedElement	Auch die normal ausgewählten Objekte hinzufügen?
	 * @return	Liste der markierten Elemente (ist nie <code>null</code>, kann aber leer sein)
	 */
	public List<ModelElement> getSelectedArea(final boolean addRegularSelectedElement) {
		return surfacePanel.getSelectedArea(addRegularSelectedElement);
	}

	/**
	 * Liefert das aktuelle selektierte Element. Ist kein Element direkt selektiert,
	 * so wird das erste Element einer Bereichsselektion geliefert.
	 * @return	Aktuell selektiertes Element oder <code>null</code>, falls nichts selektiert ist.
	 */
	public ModelElement getSelectedElementDirectOrArea() {
		final ModelElement element=getSelectedElement();
		if (element!=null) return element;

		final List<ModelElement> list=getSelectedArea(false);
		if (list.size()==1) return list.get(0);

		return null;
	}

	/**
	 * Stellt ein Callback ein, welches im Bedarfsfall (zur Anzeige von Tooltipdaten) ein Statistik-Objekt liefert
	 * @param statisticsGetter	Callback, welches ein Statistik-Objekt liefert (das Callback kann <code>null</code> sein und auch das zurückgelieferte Statistik-Objekt kann <code>null</code> sein)
	 */
	public void setStatisticsGetter(final Supplier<Statistics> statisticsGetter) {
		this.statisticsGetter=statisticsGetter;
	}

	/**
	 * Setzt aus mehreren Zeilen eine html-formatierte Statistikausgabe zusammen
	 * @param lines	Eingabezeilen
	 * @return	html-formatierte Statistikausgabe
	 * @see #getStatisticsInfoForElement(ModelElementBox)
	 */
	private String formatStatisticsData(final String[] lines) {
		if (lines==null || lines.length==0) return null;
		final StringBuilder sb=new StringBuilder();
		sb.append("<i><span style='color: green;'>");
		sb.append(Language.tr("Main.Toolbar.ShowStatistics"));
		sb.append(":<br>");
		sb.append(String.join("<br>",lines));
		sb.append("</span></i>");
		return sb.toString();
	}

	/**
	 * Setzt aus mehreren Zeilen eine html-formatierte Statistikausgabe zusammen
	 * @param lines	Eingabezeilen
	 * @return	html-formatierte Statistikausgabe
	 * @see #getStatisticsInfoForElement(ModelElementBox)
	 */
	private String formatStatisticsData(final List<String> lines) {
		return formatStatisticsData(lines.toArray(new String[0]));
	}

	/**
	 * Wandelt eine Zeile eine html-formatierte Statistikausgabe um
	 * @param line	Eingabezeile
	 * @return	html-formatierte Statistikausgabe
	 * @see #getStatisticsInfoForElement(ModelElementBox)
	 */
	private String formatStatisticsData(final String line) {
		return formatStatisticsData(new String[]{line});
	}

	/**
	 * Wandelt eine Zeitangabe in eine Zeichenkette um
	 * @param time	Zeitangabe
	 * @return	Textdarstellung der Zeitangabe
	 * @see #getStatisticsInfoForElement(ModelElementBox)
	 */
	private String formatTime(final double time) {
		return TimeTools.formatExactTime(time)+" ("+StatisticTools.formatNumber(time)+")";
	}

	/**
	 * Liefert, sofern verfügbar, Statistikdaten, die sich auf eine bestimmte
	 * Station beziehen. (Die Statistikdaten werden über {@link #statisticsGetter}
	 * bezogen und von {@link ModelSurfacePanel} verwendet, um sie in einem
	 * Tooltip anzuzeigen.
	 * @param element	Station für die die Statistikdaten zurückgegeben werden sollen
	 * @return	html-formatierte Statistikdaten oder <code>null</code>, wenn keine Daten dazur Verfügung stehen
	 * @see #statisticsGetter
	 * @see #surfacePanel
	 */
	private String getStatisticsInfoForElement(final ModelElementBox element) {
		if (statisticsGetter==null) return null;
		final Statistics statistics=statisticsGetter.get();
		if (statistics==null) return null;

		if (element instanceof ModelElementSource) {
			final String nameStation=RunElement.buildName(element,Language.tr("Simulation.Element.Source.Name"));
			final String nameClient=element.getName();
			final StatisticsDataPerformanceIndicator inter=((StatisticsDataPerformanceIndicator)statistics.clientsInterarrivalTime.getOrNull(nameStation));
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator transfer=((StatisticsDataPerformanceIndicator)statistics.clientsTransferTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator process=((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator residence=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.getOrNull(nameClient));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.getOrNull(nameClient));
			final StatisticsTimePerformanceIndicator nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByClient.getOrNull(nameClient));
			final List<String> lines=new ArrayList<>();
			if (inter!=null) lines.add("E[I]="+formatTime(inter.getMean()));
			lines.add(nameClient);
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
			if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
			if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
			if (nq!=null && nq.getTimeMean()>0) lines.add("E[NQ]="+StatisticTools.formatNumber(nq.getTimeMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+StatisticTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementAssign) {
			final String nameClient=element.getName();
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.clientsWaitingTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator transfer=((StatisticsDataPerformanceIndicator)statistics.clientsTransferTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator process=((StatisticsDataPerformanceIndicator)statistics.clientsProcessingTimes.getOrNull(nameClient));
			final StatisticsDataPerformanceIndicator residence=((StatisticsDataPerformanceIndicator)statistics.clientsResidenceTimes.getOrNull(nameClient));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsInSystemByClient.getOrNull(nameClient));
			final StatisticsTimePerformanceIndicator nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByClient.getOrNull(nameClient));
			final List<String> lines=new ArrayList<>();
			lines.add(nameClient);
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
			if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
			if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
			if (nq!=null && nq.getTimeMean()>0) lines.add("E[NQ]="+StatisticTools.formatNumber(nq.getTimeMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+StatisticTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementProcess) {
			final String nameStation=RunElement.buildName(element,Language.tr("Simulation.Element.Process.Name"));
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(nameStation));
			final StatisticsDataPerformanceIndicator transfer=((StatisticsDataPerformanceIndicator)statistics.stationsTransferTimes.getOrNull(nameStation));
			final StatisticsDataPerformanceIndicator process=((StatisticsDataPerformanceIndicator)statistics.stationsProcessingTimes.getOrNull(nameStation));
			final StatisticsDataPerformanceIndicator residence=((StatisticsDataPerformanceIndicator)statistics.stationsResidenceTimes.getOrNull(nameStation));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(nameStation));
			final StatisticsTimePerformanceIndicator nq=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationQueueByStation.getOrNull(nameStation));
			final List<String> lines=new ArrayList<>();
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (transfer!=null && transfer.getMean()>0) lines.add("E[T]="+formatTime(transfer.getMean()));
			if (process!=null && process.getMean()>0) lines.add("E[S]="+formatTime(process.getMean()));
			if (residence!=null && residence.getMean()>0) lines.add("E[V]="+formatTime(residence.getMean()));
			if (nq!=null && nq.getTimeMean()>0) lines.add("E[NQ]="+StatisticTools.formatNumber(nq.getTimeMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+StatisticTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementDelay) {
			final String nameStation=RunElement.buildName(element,Language.tr("Simulation.Element.Delay.Name"));
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(nameStation));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(nameStation));
			final List<String> lines=new ArrayList<>();
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+StatisticTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementBatch) {
			final String nameStation=RunElement.buildName(element,Language.tr("Simulation.Element.Batch.Name"));
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(nameStation));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(nameStation));
			final List<String> lines=new ArrayList<>();
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+StatisticTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementMatch) {
			final String nameStation=RunElement.buildName(element,Language.tr("Simulation.Element.Match.Name"));
			final StatisticsDataPerformanceIndicator waiting=((StatisticsDataPerformanceIndicator)statistics.stationsWaitingTimes.getOrNull(nameStation));
			final StatisticsTimePerformanceIndicator wip=((StatisticsTimePerformanceIndicator)statistics.clientsAtStationByStation.getOrNull(nameStation));
			final List<String> lines=new ArrayList<>();
			if (waiting!=null && waiting.getMean()>0) lines.add("E[W]="+formatTime(waiting.getMean()));
			if (wip!=null && wip.getTimeMean()>0) lines.add("E[N]="+StatisticTools.formatNumber(wip.getTimeMean()));
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementCounter) {
			final String groupName=((ModelElementCounter)element).getGroupName().replace('-','_')+"-";
			final StatisticsSimpleCountPerformanceIndicator indicator=((StatisticsSimpleCountPerformanceIndicator)statistics.counter.getOrNull(groupName+element.getName()));
			final long value=(indicator==null)?0:indicator.get();
			long sum=0;
			for (String name: statistics.counter.getNames()) {
				if (name.startsWith(groupName)) sum+=((StatisticsSimpleCountPerformanceIndicator)statistics.counter.get(name)).get();
			}
			return formatStatisticsData(NumberTools.formatLong(value)+" ("+StatisticTools.formatPercent(((double)value)/sum)+")");
		}

		if (element instanceof ModelElementCounterBatch) {
			final List<String> lines=new ArrayList<>();
			final String stationName=element.getName();
			for (String name: statistics.counterBatch.getNames()) {
				if (!name.startsWith(stationName)) continue;
				final StatisticsDataPerformanceIndicator indicator=(StatisticsDataPerformanceIndicator)statistics.counterBatch.get(name);
				if (indicator.getCount()==0) continue;
				if (stationName.equals(name)) {
					lines.add("E[IB]="+formatTime(indicator.getMean())+", #IB="+NumberTools.formatLong(indicator.getCount()));
				} else {
					lines.add(name.substring(stationName.length()+1)+": E[IB]="+formatTime(indicator.getMean())+", #IB="+NumberTools.formatLong(indicator.getCount()));
				}
			}
			return formatStatisticsData(lines);
		}

		if (element instanceof ModelElementDifferentialCounter) {
			final StatisticsTimePerformanceIndicator counter=(StatisticsTimePerformanceIndicator)statistics.differentialCounter.getOrNull(element.getName());
			if (counter==null) return null;
			return formatStatisticsData(Language.tr("Statistics.Average")+"="+StatisticTools.formatNumber(counter.getTimeMean()));
		}

		if (element instanceof ModelElementThroughput) {
			final StatisticsQuotientPerformanceIndicator indicator=(StatisticsQuotientPerformanceIndicator)statistics.throughputStatistics.get(element.getName());
			if (indicator==null) return null;
			double value=indicator.getQuotient();
			String unit=Language.tr("Statistics.TimeUnit.Second");
			if (value<1) {
				value*=60;
				unit=Language.tr("Statistics.TimeUnit.Minute");
				if (value<1) {
					value*=60;
					unit=Language.tr("Statistics.TimeUnit.Hour");
					if (value<1) {
						value*=24;
						unit=Language.tr("Statistics.TimeUnit.Day");
					}
				}
			}
			return formatStatisticsData(Language.tr("Statistics.Throughput")+" "+name+": "+StatisticTools.formatNumber(value,2)+" (1/"+unit+")");
		}

		return null;
	}

	/**
	 * Listener, die benachrichtigt werden, wenn der Nutzer per Kontextmenü die Erstellung einer Parameterreihe auslöst
	 * @see #fireBuildParameterSeries(ui.parameterseries.ParameterCompareTemplatesDialog.TemplateRecord)
	 */
	private final Set<Consumer<ParameterCompareTemplatesDialog.TemplateRecord>> buildParameterSeriesListeners=new HashSet<>();

	/**
	 * Fügt einen Listener zu der Liste der Listener hinzu, die benachrichtigt werden sollen, wenn der Nutzer per Kontextmenü die Erstellung einer Parameterreihe auslöst.
	 * @param listener	Zusätzlicher zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener zu der Liste der zu benachrichtigenden Listener hinzugefügt werden konnte
	 */
	public boolean addBuildParameterSeriesListener(final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> listener) {
		return buildParameterSeriesListeners.add(listener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der Listener, die benachrichtigt werden sollen, wenn der Nutzer per Kontextmenü die Erstellung einer Parameterreihe auslöst.
	 * @param listener	Nicht mehr zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener aus der Liste der zu benachrichtigenden Listener entfernt werden konnte
	 */
	public boolean removeBuildParameterSeriesListener(final Consumer<ParameterCompareTemplatesDialog.TemplateRecord> listener) {
		return buildParameterSeriesListeners.remove(listener);
	}

	/**
	 * Löst die Listener, die benachrichtigt werden, wenn der Nutzer per Kontextmenü die Erstellung einer Parameterreihe auslöst, aus.
	 * @param template	Gewählte Parameterreihen-Vorlage
	 */
	private void fireBuildParameterSeries(final ParameterCompareTemplatesDialog.TemplateRecord template) {
		buildParameterSeriesListeners.stream().forEach(listener->listener.accept(template));
	}
}