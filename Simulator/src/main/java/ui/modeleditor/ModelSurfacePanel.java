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
package ui.modeleditor;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.im.InputContext;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TooManyListenersException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.eps.EPSProcessor;
import de.erichseifert.vectorgraphics2d.intermediate.CommandSequence;
import de.erichseifert.vectorgraphics2d.util.PageSize;
import language.Language;
import mathtools.NumberTools;
import mathtools.distribution.tools.FileDropperData;
import simulator.editmodel.EditModel;
import simulator.runmodel.SimulationData;
import systemtools.BaseDialog;
import systemtools.GUITools;
import systemtools.ImageTools;
import systemtools.MsgBox;
import systemtools.statistics.PDFWriter;
import systemtools.statistics.XWPFDocumentPictureTools;
import tools.SetupData;
import ui.EditorPanel;
import ui.MainFrame;
import ui.ModelChanger;
import ui.dialogs.UndoRedoDialog;
import ui.images.Images;
import ui.modeleditor.coreelements.ModelElement;
import ui.modeleditor.coreelements.ModelElementBox;
import ui.modeleditor.coreelements.ModelElementPosition;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderSingleStation;
import ui.modeleditor.descriptionbuilder.ModelDescriptionBuilderStyled;
import ui.modeleditor.elements.AnimationExpression;
import ui.modeleditor.elements.ElementAnimationClickable;
import ui.modeleditor.elements.FontCache;
import ui.modeleditor.elements.ModelElementAnimationBar;
import ui.modeleditor.elements.ModelElementAnimationLineDiagram;
import ui.modeleditor.elements.ModelElementAnimationTextValue;
import ui.modeleditor.elements.ModelElementDashboard;
import ui.modeleditor.elements.ModelElementEdge;
import ui.modeleditor.elements.ModelElementImage;
import ui.modeleditor.elements.ModelElementInput;
import ui.modeleditor.elements.ModelElementNote;
import ui.modeleditor.elements.ModelElementSourceTable;
import ui.modeleditor.elements.ModelElementSub;
import ui.modeleditor.elements.ModelElementText;
import ui.parameterseries.ParameterCompareTemplatesDialog;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateMode;
import ui.parameterseries.ParameterCompareTemplatesDialog.TemplateRecord;
import ui.tools.GlassInfo;
import ui.tools.InputContextFix;

/**
 * Zeichenfläche für die Modell-Elemente<br>
 * Achtung: <code>ModelSurfacePanel</code> sollte als übergeordnetes Element in ein <code>JScrollPane</code> eingebettet werden.
 * @author Alexander Herzog
 */
public final class ModelSurfacePanel extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2829918440976903115L;

	/**
	 * Erlaubt das Bearbeiten von Elementen sowie das Verschieben im ansonsten Read-Only-Modus.
	 * Das Löschen von Elementen und das Erstellen neuer Elemente bleibt aber verboten.
	 * @see #allowEditorDialogs()
	 */
	private boolean allowChangeOperationsOnReadOnly=false;

	/**
	 * Größe der Zeichenfläche
	 */
	public static final int SIZE=100_000;

	/**
	 * Minimaler Zoomfaktor
	 */
	public static final double ZOOM_MIN=0.2;

	/**
	 * Maximaler Zoomfaktor
	 */
	public static final double ZOOM_MAX=4;

	/**
	 * Änderung des Zoomfaktors pro Mausrad-Schritt
	 */
	public static final double ZOOM_STEP=0.2;

	/**
	 * Anteil einer Bildschirmseite, um die das Fenster bei einem Mausrad-Schritt gescrollt werden soll
	 */
	public static final double WHEEL_SCROLL_FRACTION=25;

	/**
	 * Maximale Undo-Schritte
	 */
	public static final int MAX_UNDO_STEPS=20;

	/**
	 * Minimaler Abstand in Sekunden zwischen der Aufzeichnung von zwei Undo-Schritten
	 */
	public static final int MIN_UNDO_TIME_DELTA=1;

	/**
	 * Betriebsart der Zeichenfläche
	 * @see ModelSurfacePanel#getMode()
	 * @see ModelSurfacePanel#startAddElement(ModelElementPosition)
	 * @see ModelSurfacePanel#pasteFromClipboard()
	 * @see ModelSurfacePanel#clipboardData
	 */
	public enum ClickMode {
		/** Betriebsmodus: Normal */
		MODE_NORMAL,

		/** Betriebsmodus: Element hinzufügen */
		MODE_ADD_ELEMENT,

		/** Betriebsmodus: Kante hinzufügen (Auswahl des ersten zu verbindenden Elements) */
		MODE_ADD_EDGE_STEP1,

		/** Betriebsmodus: Kante hinzufügen (Auswahl des zweiten zu verbindenden Elements) */
		MODE_ADD_EDGE_STEP2,

		/** Betriebsmodus: Inhalt der Zwischenablage (Elemente) einfügen */
		MODE_INSERT_ELEMENTS_FROM_CLIPBOARD,

		/** Betriebsmodus: Elemente aus Vorlage einfügen */
		MODE_INSERT_ELEMENTS_FROM_TEMPLATE,

		/** Betriebsmodus: Inhalt der Zwischenablage (Text) einfügen */
		MODE_INSERT_TEXT,

		/** Betriebsmodus: Inhalt der Zwischenablage (Bild) einfügen */
		MODE_INSERT_IMAGE
	}

	/**
	 * Modus zum automatischen Hinzufügen von Verbindungskanten
	 * @see ModelSurfacePanel#getAutoConnect()
	 * @see ModelSurfacePanel#autoConnect(ModelElement, ModelElement)
	 */
	public enum ConnectMode {
		/** Neue Elemente nicht automatisch verbinden */
		OFF,

		/** Neues Element jeweils mit dem vorherigen verbinden */
		AUTO,

		/** Nächstliegendes Element suchen und verbinden */
		SMART
	}

	/** Ist die Rückgängig-Funktion verfügbar? ({@link #disableUndo()} */
	private boolean canUndo=true;
	/** Nur-Lese-Status */
	private final boolean readOnly;
	/** Darf der Menüpunkt "Modelleigenschaften" im Kontextmenü angezeigt werden? */
	private boolean showEditModelProperties=true;
	/** Gibt an, ob gerade eine Operation, die evtl. mehrere Elemente umfasst, läuft und daher Statusausgaben usw. zunächst nicht verarbeitet werden sollen. */
	private boolean operationRunning=false;

	/** Zugehöriges Modell */
	private transient EditModel model=new EditModel();
	/** Zeichenfläche die dargestellt werden soll */
	private transient ModelSurface surface=null;
	/** Kundendaten aus {@link #model} */
	private transient ModelClientData clientData;
	/** Fertigungspläne aus {@link #model} */
	private transient ModelSequences sequences;
	/** Optionale (Animations-)Simulationsdaten ({@link #setAnimationSimulationData(SimulationData, ModelSurfaceAnimator)}) */
	private transient SimulationData simData;
	/** Optionales Animationssystem-Objekt der Hauptebene ({@link #setAnimationSimulationData(SimulationData, ModelSurfaceAnimator)}) */
	private transient ModelSurfaceAnimator mainAnimator;

	/** Reagiert auf {@link ModelSurface#addRedrawListener(Runnable)} */
	private final transient Runnable requestRedrawListener;
	/** Reagiert auf {@link ModelSurface#addRequestCopyListener(Runnable)} */
	private final transient Runnable requestCopyListener;
	/** Reagiert auf {@link ModelSurface#addRequestCutListener(Runnable)} */
	private final transient Runnable requestCutListener;
	/** Aktueller Zoomfaktor */
	private double zoom;
	/** Aktueller Raster-Anzeige-Modus */
	private ModelSurface.Grid raster;
	/** Farben für den Hintergrund */
	private Color[] colors=new Color[]{ModelSurface.DEFAULT_BACKGROUND_COLOR,ModelSurface.DEFAULT_RASTER_COLOR};
	/** Optionales Hintergrundbild */
	private BufferedImage backgroundImage;
	/** Hashwert des Hintergrundbildes {@link #backgroundImage} (wird automatisch in {@link #setBackgroundImage(BufferedImage, double, ui.modeleditor.ModelSurface.BackgroundImageMode)} gesetzt */
	private String backgroundImageHash;
	/** Skalierungsfaktor für das optionale Hintergrundbild */
	private double backgroundImageScale;
	/** Soll das Hintergrundbild, sofern {@link #backgroundImage} ungleich <code>null</code> ist, überhaupt gezeichnet werden? */
	private ModelSurface.BackgroundImageMode backgroundImageMode;
	/** Mausposition beim Start einer Drag&amp;drop-Operation */
	private Point dragStartMousePosition=null;
	/** Per Drag&amp;drop zu verschiebendes Element */
	private transient ModelElement dragElement;
	/** Per Drag&amp;drop zu kopierendes Element */
	private transient ModelElement dragCopyElement;
	/** Eckpunkt an dem ein Element in der Größe verändert wird */
	private int dragStartElementBorderPointNr;
	/** Startposition für die Größenänderung eines Elements */
	private Point dragStartElementPosition;
	/** Auswahlbereich */
	private Rectangle selectBox;

	/**
	 * Aktueller Betriebsmodus
	 * @see #getMode()
	 * @see #setMode(ClickMode)
	 */
	private ClickMode mode=ClickMode.MODE_NORMAL;

	/** Hinzuzufügendes Element */
	private transient ModelElement modeAddElement;
	/** Element von dem aus nach dem Einfügen eines neuen Elements eine Verbindung zu dem neuen Element hergestellt werden soll */
	private transient ModelElementBox modeAddElementSource;
	/** Datenobjekt zum Einfügen aus der Zwischenablage */
	private transient ByteArrayInputStream clipboardData;
	/** Text zum Einfügen aus der Zwischenablage */
	private String clipboardText;
	/** Bild zum Einfügen aus der Zwischenablage */
	private transient BufferedImage clipboardImage;

	/**
	 * Listener, die benachrichtigt werden sollen, wenn sich der Selektionmodus ändert
	 * @see #fireStateChangeListener()
	 */
	private final transient List<ActionListener> stateChangeListener;

	/**
	 * Listener, die benachrichtigt werden sollen, wenn sich die Verfügbarkeit von Undo/Redo-Schritten ändert
	 * @see #fireUndoRedoDoneListener()
	 */
	private final transient List<ActionListener> undoRedoDoneListener;

	/**
	 * Listen, die über Änderungen am Zoomfaktor benachrichtigt werden sollen
	 */
	private final transient List<ActionListener> zoomChangeListeners;

	/**
	 * Hält das Cursor-Objekt für "Verknüpfung hier nicht möglich" vor
	 */
	private transient Cursor cursorNotAllowed;

	/**
	 * Kopie des letzten Status der Zeichenflächendaten
	 * zu Erkennung, ob es Veränderungen gab
	 */
	private transient ModelSurface lastSurfaceState=null;

	/**
	 * Zeitpunkt (in System-Millisekunden) an dem die Zeichenfläche
	 * das letzte Mal auf Änderungen überprüft wurde, um so prüfen
	 * zu können, ob {@link #MIN_UNDO_TIME_DELTA} überschritten  wurde
	 * und ggf. ein weiterer Rückgängig-Schritt erfasst werden soll.
	 */
	private long lastSurfaceStateTime=0;

	/**
	 * Liste mit Zeichenflächen-Puffer für Rückgängig-Operationen
	 */
	private final transient List<ModelSurface> undoBuffer;

	/**
	 * Liste mit Zeichenflächen-Puffer für Wiederholen-Operationen
	 */
	private final transient List<ModelSurface> redoBuffer;

	/**
	 * Optionales UserPaint-Objekt, welches beim Zeichnen zusätzlich aufgerufen wird
	 * @see #setAdditionalUserPaint(UserPaint)
	 */
	private transient UserPaint additionalUserPaint;

	/**
	 * Informationen zur linken Symbolleiste auf der leeren Zeichenfläche anzeigen?
	 */
	private final boolean useInfoPaint;

	/**
	 * Optionales Callback, das zu einem Element weitere Tooltip-Daten (aus der Statistik) liefert
	 * @see #setAdditionalTooltipGetter(Function)
	 */
	private transient Function<ModelElementBox,String> additionalTooltipGetter;

	/**
	 * Gibt an, ob Kanten zu neu eingefügten Elementen wenn möglich automatisch hinzugefügt werden sollen.
	 * @see #getAutoConnect()
	 * @see #setAutoConnect(ConnectMode)
	 */
	private ConnectMode autoConnect=ConnectMode.OFF;

	/** Vertikale Positionen für die Infotexte auf der Zeichenfläche */
	private transient int[] infoPositions;
	/** Klick-Bereiche für die Infotexte auf der Zeichenfläche */
	private transient Rectangle[] linkPositions;

	/**
	 * Konstruktor der Klasse <code>ModelSurfacePanel</code><br>
	 * Achtung: <code>ModelSurfacePanel</code> sollte als übergeordnetes Element in ein <code>JScrollPane</code> eingebettet werden.
	 * @param readOnly	Legt fest, ob Änderungen an dem Modell vorgenommen werden dürfen.
	 * @param useInfoPaint	Informationen zur linken Symbolleiste auf der leeren Zeichenfläche anzeigen
	 */
	public ModelSurfacePanel(final boolean readOnly, final boolean useInfoPaint) {
		super();
		this.readOnly=readOnly;
		this.useInfoPaint=useInfoPaint;
		stateChangeListener=new ArrayList<>();
		undoRedoDoneListener=new ArrayList<>();
		zoomChangeListeners=new ArrayList<>();
		undoBuffer=new ArrayList<>();
		redoBuffer=new ArrayList<>();

		setZoom(SetupData.getSetup().lastZoom);
		raster=ModelSurface.Grid.LINES;

		requestRedrawListener=()->repaint();
		requestCopyListener=()->copyToClipboard();
		requestCutListener=()->{copyToClipboard(); deleteSelectedElements();};

		addMouseListener(new ModelSurfacePanelMouseListener());
		addMouseMotionListener(new ModelSurfacePanelMouseMotionListener());
		addMouseWheelListener(new ModelSurfacePanelMouseWheelListener());
		addKeyListener(new ModelSurfacePanelKeyListener());

		setFocusable(true);
		SwingUtilities.invokeLater(()->{requestFocus(); requestFocusInWindow();});

		if (!GraphicsEnvironment.isHeadless()) try {
			final BufferedImage image=ImageIO.read(Images.MODELEDITOR_NOT_ALLOWED_EDGE.getURLs()[0]);
			cursorNotAllowed=Toolkit.getDefaultToolkit().createCustomCursor(image,new Point(image.getWidth()/2,image.getHeight()/2),"NotAllowed");
		} catch (IOException e) {cursorNotAllowed=null;}
		if (cursorNotAllowed==null) cursorNotAllowed=Cursor.getDefaultCursor();
	}

	/**
	 * Konstruktor der Klasse <code>ModelSurfacePanel</code><br>
	 * Achtung: <code>ModelSurfacePanel</code> sollte als übergeordnetes Element in ein <code>JScrollPane</code> eingebettet werden.
	 */
	public ModelSurfacePanel() {
		this(false,false);
	}

	@Override
	public InputContext getInputContext() {
		return new InputContextFix(super.getInputContext());
	}

	/**
	 * Deaktiviert das Undo/Redo-System für diese Zeichenfläche.<br>
	 * Dies sollte über den Konstruktor von {@link EditorPanel} für den Fall,
	 * dass es sich um einen Viewer oder eine Unter-Zeichenfläche handelt,
	 * durchgeführt werden.
	 */
	public void disableUndo() {
		canUndo=false;
	}

	/**
	 * Legt fest, ob der Menüpunkt "Modelleigenschaften" im Kontextmenü angezeigt werden darf
	 * @param showEditModelProperties	Soll der Menüpunkt "Modelleigenschaften" im Kontextmenü angezeigt werden?
	 */
	public void setShowEditModelProperties(final boolean showEditModelProperties) {
		this.showEditModelProperties=showEditModelProperties;
	}

	/**
	 * Setzt das {@link ModelSurface}-Element welches die Modell-Elemente beinhaltet;
	 * dabei wird kein Undo-Schritt erfasst
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Darzustellendes <code>ModelSurface</code>-Element
	 */
	private void setSurfaceNoUndoCheck(final EditModel model, final ModelSurface surface) {
		if (surface==this.surface) return;

		final EditorPanel editorPanel=(this.surface==null)?null:this.surface.getEditorPanel();

		if (this.surface!=null) {
			this.surface.removeRedrawListener(requestRedrawListener);
			this.surface.removeRequestCopyListener(requestCopyListener);
			this.surface.removeRequestCutListener(requestCutListener);
		}
		this.model=model;
		this.surface=surface;
		if (this.surface!=null) {
			this.surface.setHeatMapIntensityGetter(heatMapIntensityGetter);
			this.surface.addRedrawListener(requestRedrawListener);
			this.surface.addRequestCopyListener(requestCopyListener);
			this.surface.addRequestCutListener(requestCutListener);
			model.surface=surface;
			this.surface.setEditorPanel(editorPanel);
		}
		SwingUtilities.invokeLater(()->{
			repaint();
			fireStateChangeListener();
		});
	}

	/**
	 * Setzt das {@link ModelSurface}-Element welches die Modell-Elemente beinhaltet
	 * @param model	Element vom Typ <code>EditModel</code> (wird benötigt, um die Liste der globalen Variablen zu laden)
	 * @param surface	Darzustellendes <code>ModelSurface</code>-Element
	 * @param clientData	Kundendaten-Objekt (beim Ändern der Namen im Surface werden hier ggf. neue Kundentypen als Kopien von vorhandenen angelegt)
	 * @param sequences	Fertigungspläne-Objekt
	 */
	public void setSurface(final EditModel model, final ModelSurface surface, final ModelClientData clientData, final ModelSequences sequences) {
		setSurfaceNoUndoCheck(model,surface);
		surface.addStateChangeListener(()->fireStateChangeListener());
		if (clientData!=null) this.clientData=clientData;
		if (sequences!=null) this.sequences=sequences;
		checkIfModelChanged(false);
	}

	/**
	 * Liefert das aktuelle <code>ModelSurface</code>-Element zurück
	 * @return	Dargestelltes <code>ModelSurface</code>-Element
	 */
	public ModelSurface getSurface() {
		return surface;
	}

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn sich der Zoomfaktor ändert
	 * @param zoomChangeListener	Zu benachrichtigender Listener
	 */
	public void addZoomChangeListener(final ActionListener zoomChangeListener) {
		if (zoomChangeListeners.indexOf(zoomChangeListener)<0) zoomChangeListeners.add(zoomChangeListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer Änderung des Zoomfaktors zu benachrichtigenden Listener
	 * @param zoomChangeListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeZoomChangeListener(final ActionListener zoomChangeListener) {
		return zoomChangeListeners.remove(zoomChangeListener);
	}

	/**
	 * Benachrichtigt die Listen, dass der Zoomfaktor geändert wurde
	 * @see #zoomChangeListeners
	 */
	private void fireZoomChangeListeners() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,NumberTools.formatNumber(getZoom()));
		for (ActionListener listener: zoomChangeListeners) listener.actionPerformed(event);
	}

	/**
	 * Liefert den aktuell eingestellten Zoomfaktor
	 * @return Aktueller Zoomfaktor
	 */
	public double getZoom() {
		return zoom;
	}

	/**
	 * Verringert den Zoomfaktor.
	 */
	public void zoomOut() {
		setZoom(getZoom()-ZOOM_STEP);
	}

	/**
	 * Vergrößert den Zoomfaktor.
	 */
	public void zoomIn() {
		setZoom(getZoom()+ZOOM_STEP);
	}

	/**
	 * Stellt den Standard-Zoomfaktor wieder her.
	 */
	public void zoomDefault() {
		setZoom(1);
	}

	/**
	 * Stellt den Zoomfaktor ein
	 * @param zoom	Neuer Zoomfaktor
	 */
	public void setZoom(final double zoom) {
		if (zoom<0.01 || zoom>10) return;
		if (zoom==this.zoom) return;
		this.zoom=zoom;
		fireZoomChangeListeners();
		final SetupData setup=SetupData.getSetup();
		if (zoom!=setup.lastZoom) {
			setup.lastZoom=zoom;
			setup.saveSetup();
		}
		repaint();
	}

	/**
	 * Zentriert das Modell auf der Zeichenfläche
	 */
	public void centerModel() {
		if (surface==null || !(getParent() instanceof JViewport)) return;

		final Point point=surface.getModelCenter();

		final JViewport viewport=(JViewport)getParent();
		final Rectangle viewArea=viewport.getViewRect();

		point.translate(-(int)Math.round(viewArea.width/zoom/2.0),-(int)Math.round(viewArea.height/zoom/2.0));
		point.x=Math.max(0,point.x);
		point.y=Math.max(0,point.y);

		viewport.setViewPosition(point);

		repaint();
	}

	/**
	 * Scrollt ganz nach oben links.
	 */
	public void scrollToTop() {
		if (surface==null || !(getParent() instanceof JViewport)) return;

		final JViewport viewport=(JViewport)getParent();
		viewport.setViewPosition(new Point(0,0));

		repaint();
	}

	/**
	 * Liefert die Position der linken oberen Ecke.
	 * @return	Position der linken oberen Ecke (oder <code>null</code>, wenn diese nicht ermittelt werden konnte)
	 * @see #setTopPosition(Point)
	 */
	public Point getTopPosition() {
		if (surface==null || !(getParent() instanceof JViewport)) return null;
		final JViewport viewport=(JViewport)getParent();
		return viewport.getViewPosition();
	}

	/**
	 * Stellt die Position der linken oberen Ecke ein.
	 * @param topPosition	Position der linken oberen Ecke
	 * @see #getTopPosition()
	 */
	public void setTopPosition(final Point topPosition) {
		if (topPosition==null) return;
		if (surface==null || !(getParent() instanceof JViewport)) return;
		final JViewport viewport=(JViewport)getParent();
		viewport.setViewPosition(topPosition);
	}

	/**
	 * Zeigt den Dialog zur Konfiguration des Diagramme-Dashboards an.
	 */
	public void showDashboard() {
		final ModelElementDashboard dashboard;

		final boolean isInSurface;

		/* Schon ein entsprechendes Element vorhanden? */
		final Optional<ModelElementDashboard> dashboardOptional=surface.getElements().stream().filter(element->element instanceof ModelElementDashboard).map(element->((ModelElementDashboard)element)).findFirst();
		if (dashboardOptional.isPresent()) {
			dashboard=dashboardOptional.get();
			isInSurface=true;
		} else {
			/* Ggf. neues Element anlegen */
			dashboard=new ModelElementDashboard(model,surface);
			isInSurface=false;
		}

		dashboard.showSubEditDialog(this,false,false);

		if (dashboard.getSubSurface().getElementCount()==0) {
			if (isInSurface) surface.remove(dashboard);
		} else {
			if (!isInSurface) surface.add(dashboard);
		}
	}

	/**
	 * Gibt an, ob Änderungen an dem Modell vorgenommen werden dürfen.
	 * @return	Nur-Lese-Status
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Gibt an, ob ein Raster auf der Zeichenfläche angezeigt wird
	 * @return	Rasteranzeige
	 */
	public ModelSurface.Grid getRaster() {
		return raster;
	}

	/**
	 * Stellt ein, ob ein Raster auf der Zeichenfläche angezeigt wird
	 * @param raster	Rasteranzeige
	 */
	public void setRaster(final ModelSurface.Grid raster) {
		if (raster==this.raster || raster==null) return;
		this.raster=raster;
		repaint();
	}

	/**
	 * Listener, die benachrichtigt werden sollen, wenn eine Datei auf der Komponente abgelegt wird
	 * @see #dropFile(File, Point)
	 */
	private List<ActionListener> fileDropListeners=new ArrayList<>();

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
	 * Reagiert auf das Ablegen einer Datei auf der Zeichenfläche
	 * @param file	Per Drag&amp;drop abgelegte Datei
	 * @param point	Position auf der Zeichenfläche
	 */
	private void dropFile(final File file, final Point point) {
		BufferedImage image=null;
		try {image=ImageIO.read(file);} catch (IOException e) {image=null;}
		if (image!=null) {
			ModelElementImage element=new ModelElementImage(model,surface);
			element.setImage(image);
			int x,y;
			if (point==null) {
				JViewport viewport=(JViewport)getParent();
				final Point p=viewport.getViewPosition();
				x=(int)Math.round((p.x+5)/zoom/5)*5;
				y=(int)Math.round((p.y+5)/zoom/5)*5;
			} else {
				x=(int)Math.round(point.x/zoom/5)*5;
				y=(int)Math.round(point.y/zoom/5)*5;
			}
			element.setPosition(new Point(x,y));
			surface.add(element);
			setMode(ClickMode.MODE_NORMAL);
		} else {
			final ActionEvent event=FileDropperData.getActionEvent(null,file,this,point);
			for (ActionListener listener: fileDropListeners) listener.actionPerformed(event);
		}
	}

	/**
	 * Stellt ein, ob das Objekt per Drag&amp;Drop übertragene <code>ModelElementPosition</code>-Vorlageelemente annehmen soll.
	 * @param acceptTemplateElements	Wird <code>true</code> übergeben, so werden Vorlagen per Drag&amp;Drop angenommen.
	 */
	public void setDropAccept(final boolean acceptTemplateElements) {
		if (acceptTemplateElements) {
			final DropTarget dt=new DropTarget();
			setDropTarget(dt);
			try {
				dt.addDropTargetListener(new DropTargetAdapter() {
					private boolean dropFileList(final DropTargetDropEvent dtde, final Transferable transfer) {
						try {
							final Object obj=transfer.getTransferData(DataFlavor.javaFileListFlavor);
							if (obj instanceof List) for (Object entry: ((List<?>)obj)) if (entry instanceof File) {
								dropFile((File)entry,dtde.getLocation());
								break;
							}
							return true;
						} catch (UnsupportedFlavorException | IOException e) {return false;}
					}

					private boolean dropElement(final DropTargetDropEvent dtde, final Transferable transfer) {
						try {
							final Object obj=transfer.getTransferData(DataFlavor.stringFlavor);
							if (!(obj instanceof String)) return false;
							final ModelElementPosition element=ModelElementCatalog.getCatalog().getMenuElement((String)obj);
							if (element!=null) {
								final Point point=dtde.getLocation();
								addElement(element,new Point(point.x+10,point.y+10));
							}
							return true;
						} catch (UnsupportedFlavorException | IOException e) {return false;}
					}

					@Override
					public void drop(final DropTargetDropEvent dtde) {
						dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE); /* Sonst können keine Dateien abgelegt werden */
						final Transferable transfer=dtde.getTransferable();

						/* Datei(en) abgelegt */
						if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
							if (dropFileList(dtde,transfer)) return;
						}

						/* Modell-Element aus dem Vorlagenbereich */
						if (transfer.isDataFlavorSupported(DataFlavor.stringFlavor)) {
							if (dropElement(dtde,transfer)) return;
						}
						dtde.rejectDrop();
					}
				});
			} catch (TooManyListenersException e1) {}
		} else {
			setDropTarget(null);
		}
	}

	/**
	 * Liefert den aktuellen Betriebsmodus zurück.
	 * @return	Aktueller Betriebsmodus
	 */
	public ClickMode getMode() {
		return mode;
	}

	/**
	 * Stellt den aktuellen Betriebsmodus ein.
	 * @param mode	Neuer Betriebsmodus
	 * @see #getMode()
	 */
	private void setMode(final ClickMode mode) {
		boolean fireStateChange=(this.mode!=mode);
		this.mode=mode;
		if (mode==ClickMode.MODE_ADD_ELEMENT) {
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		} else {
			setCursor(Cursor.getDefaultCursor());
			modeAddElementSource=null;
		}
		if (fireStateChange) fireStateChangeListener();
	}

	/**
	 * Startet die verzögerte Reaktion auf Änderungen an dem Modell
	 * (bzw. hält die Auslösung der Listener zurück)
	 * @see #finishDelayStateChangeListener()
	 */
	private void startDelayStateChangeListener() {
		delayStateChangeListener=true;
		needToFireStateChangeListener=false;
	}

	/**
	 * Beendet die per {@link #startDelayStateChangeListener()}
	 * gestartete Verzögerung der {@link #stateChangeListener}
	 * und löst diese falls mittlerweile nötig aus.
	 * @see #startDelayStateChangeListener()
	 */
	private void finishDelayStateChangeListener() {
		delayStateChangeListener=false;
		if (needToFireStateChangeListener) fireStateChangeListener();
		needToFireStateChangeListener=false;
	}

	/**
	 * Soll die Auslösung der {@link #stateChangeListener} verzögert werden?
	 * @see #fireStateChangeListener()
	 * @see #startDelayStateChangeListener()
	 * @see #finishDelayStateChangeListener()
	 */
	private boolean delayStateChangeListener;

	/**
	 * Gibt es momentan zurückgehaltene {@link #stateChangeListener}-Auslösungen?
	 * @see #fireStateChangeListener()
	 * @see #startDelayStateChangeListener()
	 * @see #finishDelayStateChangeListener()
	 */
	private boolean needToFireStateChangeListener;

	/**
	 * Löst die Listener, die benachrichtigt werden sollen, wenn sich der Selektionmodus ändert, aus.
	 * @see #stateChangeListener
	 */
	private void fireStateChangeListener() {
		if (delayStateChangeListener) {needToFireStateChangeListener=true; return;}
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"statechange");
		for (ActionListener listener: stateChangeListener) listener.actionPerformed(event);
	}

	/**
	 * Fügt einen Listener, der benachrichtigt wird, wenn sich der Selektionmodus ändert, zu der Liste aller Listener hinzu.
	 * @param listener	Hinzuzufügender Listener
	 */
	public void addStateChangeListener(final ActionListener listener) {
		if (stateChangeListener.indexOf(listener)<0) stateChangeListener.add(listener);
	}

	/**
	 * Entfernt einen Listener, der benachrichtigt wird, wenn sich der Selektionmodus ändert, aus der Liste aller Listener.
	 * @param listener	Zu entfernender Listener
	 * @return	Gibt <code>true</code> zurück, wenn sich der Listener bisher in der Liste befand
	 */
	public boolean removeStateChangeListener(final ActionListener listener) {
		return stateChangeListener.remove(listener);
	}

	/**
	 * Löst die Listener, die benachrichtigt werden sollen, wenn sich die Verfügbarkeit von Undo/Redo-Schritten ändert, aus.
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
	 * Öffnet den Bearbeiten-Dialog für das gewählte Element.
	 */
	public void editSelectedElement() {
		showElementProperties(surface.getSelectedElement(),0);
	}

	/**
	 * Löscht die gewählten Elemente.
	 */
	public void deleteSelectedElements() {
		if (!readOnly) {
			startDelayStateChangeListener();
			try {
				final List<ModelElement> list=surface.getSelectedArea(true);
				if (list!=null && list.size()>0) {
					for (ModelElement listElement: new ArrayList<>(list)) surface.remove(listElement);
					fireStateChangeListener();
				}

				final ModelElement element=surface.getSelectedElement();
				if (element!=null) {
					surface.remove(element);
					fireStateChangeListener();
				}
			} finally {
				finishDelayStateChangeListener();
			}
		}
	}

	/**
	 * Wählt alle Elemente aus.
	 */
	public void selectAll() {
		surface.setSelectedElement(null);
		operationRunning=true;
		try {
			surface.setSelectedAreaAll();
		} finally {
			operationRunning=false;
		}
		fireSelectionListener();
	}

	/**
	 * Scrollt zu einem bestimmten Element und wählt es aus
	 * @param id	ID des auszuwählenden Elements
	 */
	public void selectAndScrollToElement(final int id) {
		if (id<0) return;
		if (surface==null || !(getParent() instanceof JViewport)) return;

		ModelElement element=surface.getById(id);
		if (element==null) return;
		if (!surface.isVisibleOnLayer(element)) return;
		surface.setSelectedElement(element);
		fireSelectionListener();

		Point point=element.getMiddlePosition(false);
		if (point==null) point=element.getPosition(false);
		if (point!=null) {

			JViewport viewport=(JViewport)getParent();
			Rectangle viewArea=viewport.getViewRect();

			point.translate(-viewArea.width/2,-viewArea.height/2);
			point.x=Math.max(0,point.x);
			point.y=Math.max(0,point.y);
			viewport.setViewPosition(point);
		}

		repaint();
	}

	/**
	 * Bricht das Hinzufügen eines Elements oder einer Kante ab.
	 */
	public void cancelAdd() {
		setMode(ClickMode.MODE_NORMAL);
	}

	/**
	 * Beginnt mit dem Hinzufügen keiner Verbindungskante.
	 */
	public void startAddEdge() {
		if (readOnly) return;
		surface.setSelectedElement(null);
		fireSelectionListener();
		setMode(ClickMode.MODE_ADD_EDGE_STEP1);
	}

	/**
	 * Beginnt mit dem Hinzufügen eines Elements (welches keine Kante ist).
	 * Nach dem Einfügen soll es mit dem Ausgangselement verbunden werden.
	 * @param connectionSource	Ausgangselement, mit dem das neue Element verbunden werden soll
	 * @param element	Vorlage für das hinzuzufügende Element
	 */
	public void startAddElement(final ModelElementBox connectionSource, final ModelElementPosition element) {
		startAddElement(element);
		modeAddElementSource=connectionSource;
	}

	/**
	 * Beginnt mit dem Hinzufügen eines Elements (welches keine Kante ist).
	 * @param element	Vorlage für das hinzuzufügende Element
	 */
	public void startAddElement(final ModelElementPosition element) {
		if (readOnly) return;
		surface.setSelectedElement(null);
		fireSelectionListener();
		modeAddElement=element;
		modeAddElementSource=null;
		setMode(ClickMode.MODE_ADD_ELEMENT);
	}

	/**
	 * Versucht zwei Stationen über eine Verbindungskante
	 * zu verbinden (Funktion zur automatischen Verbindung neuer Stationen).
	 * @param element1	Ausgangsstation
	 * @param element2	Zielstation
	 * @return	Liefert <code>true</code>, wenn eine Verbindungskante eingefügt werden konnte
	 * @see #addElement(ModelElementPosition, Point)
	 */
	private boolean autoConnect(final ModelElement element1, final ModelElement element2) {
		if (!(element1 instanceof ModelElementPosition)) return false;
		if (!(element2 instanceof ModelElementPosition)) return false;

		final ModelElementPosition box1=(ModelElementPosition)element1;
		final ModelElementPosition box2=(ModelElementPosition)element2;

		if (!box1.canAddEdgeOut() || !box2.canAddEdgeIn()) return false;

		final ModelElementEdge edge=new ModelElementEdge(model,surface,box1,box2);
		if (!box1.addEdgeOut(edge)) return false;
		if (!box2.addEdgeIn(edge)) {box1.removeConnectionNotify(edge); return false;}
		surface.add(edge);

		return true;
	}

	/**
	 * Versucht zwei Stationen über eine Verbindungskante
	 * zu verbinden (Funktion zur intelligenten Verbindung neuer Stationen).
	 * @param lastSelected	Zuletzt selektierte Station (wird in Kombination mit dem Abstand berücksichtigt; die Verbindung muss also nicht notwendig von dieser Station ausgehen)
	 * @param element	Zielstation
	 * @return	Liefert <code>true</code>, wenn eine Verbindungskante eingefügt werden konnte
	 * @see #addElement(ModelElementPosition, Point)
	 */
	private boolean smartConnect(final ModelElement lastSelected, final ModelElement element) {
		if (!(element instanceof ModelElementPosition)) return false;
		final ModelElementPosition box=(ModelElementPosition)element;
		if (!box.canAddEdgeIn()) return false;

		ModelElementPosition bestElement=null;
		double bestDistance=Double.MAX_VALUE;
		final Point boxPoint=box.getPosition(true);

		for (ModelElement sourceVertex: surface.getElements()) {
			if (sourceVertex==element) continue;
			if (!(sourceVertex instanceof ModelElementPosition)) continue;
			final ModelElementPosition sourceBox=(ModelElementPosition)sourceVertex;
			if (!sourceBox.canAddEdgeOut()) continue;

			final Point sourcePoint=sourceBox.getPosition(true);
			final double x=(sourcePoint.x-boxPoint.x);
			final double y=(sourcePoint.y-boxPoint.y);
			double distance=Math.sqrt(x*x+y*y);
			if (sourceBox==lastSelected) distance*=0.9; /* Das zu letzt gewählte Element bekommt einen kleinen Vorsprung. */
			if (distance<bestDistance) {
				bestDistance=distance;
				bestElement=sourceBox;
			}
		}

		if (bestElement!=null) return autoConnect(bestElement,box);
		return false;
	}

	/**
	 * Fügt ein Element in das Modell ein.
	 * @param template	Vorlage, von der eine Kopie eingefügt werden soll
	 * @param point	Position, an der das Element eingefügt werden soll
	 */
	public void addElement(final ModelElementPosition template, final Point point) {
		if (readOnly) return;
		if (surface==null) return;

		surface.setSelectedElement(null);
		fireSelectionListener();

		final ModelElement element=template.clone(model,surface);
		int x=(int)Math.round(point.x/zoom/5)*5;
		int y=(int)Math.round(point.y/zoom/5)*5;
		element.setPosition(new Point(x,y));
		surface.add(element);

		switch (autoConnect) {
		case OFF:
			/* Nichts */
			break;
		case AUTO:
			autoConnect(surface.getLastSelectedPositionElement(),element);
			break;
		case SMART:
			smartConnect(surface.getLastSelectedPositionElement(),element);
			break;
		default:
			break;
		}

		setMode(ClickMode.MODE_NORMAL);
		surface.setSelectedElement(element);
		fireSelectionListener();
		fireStateChangeListener();
	}

	/**
	 * Wird aufgerufen, wenn das Modell verändert wurde.
	 * @param previousModel	Vorheriges Modell (für die Rückgängig-Funktion)
	 */
	private void modelChanged(final ModelSurface previousModel) {
		if (!canUndo) return;
		undoBuffer.add(previousModel.clone(false,previousModel.getResources().clone(),previousModel.getSchedules().clone(),previousModel.getParentSurface(),null));
		while (undoBuffer.size()>MAX_UNDO_STEPS) undoBuffer.remove(0);

		redoBuffer.clear();

		fireUndoRedoDoneListener();
	}

	/**
	 * Prüft, ob das Modell verändert wurde
	 * @param forceCheck	Prüfung erzwingen
	 */
	private void checkIfModelChanged(final boolean forceCheck) {
		if (!canUndo || readOnly) return;
		if ((forceCheck || (mode==ClickMode.MODE_NORMAL && dragStartElementPosition==null)) && surface!=null) {

			if (lastSurfaceState!=null && lastSurfaceStateTime+MIN_UNDO_TIME_DELTA*1000<System.currentTimeMillis() && !surface.equalsModelSurface(lastSurfaceState)) {
				modelChanged(lastSurfaceState);
				lastSurfaceState=null;
			}
		}
		if (lastSurfaceState==null && surface!=null) {
			lastSurfaceState=surface.clone(false,surface.getResources().clone(),surface.getSchedules().clone(),surface.getParentSurface(),null);
			lastSurfaceStateTime=System.currentTimeMillis();
		}
	}

	/**
	 * Gibt an, ob Rückgängig-Schritte vorhanden sind
	 * @return	Gibt <code>true</code> zurück, wenn die Rückgängig-Funktion zur Verfügung steht
	 * @see #doUndo()
	 */
	public boolean canUndo() {
		return undoBuffer.size()>0;
	}

	/**
	 * Gibt an, ob Wiederholen-Schritte vorhanden sind
	 * @return	Gibt <code>true</code> zurück, wenn die Wiederholen-Funktion zur Verfügung steht
	 * @see #doRedo()
	 */
	public boolean canRedo() {
		return redoBuffer.size()>0;
	}

	/**
	 * Führt (wenn möglich) einen Rückgängig-Schritt aus
	 * @see #canUndo()
	 */
	public void doUndo() {
		if (undoBuffer.size()==0) return;

		redoBuffer.add(surface.clone(false,surface.getResources(),surface.getSchedules().clone(),surface.getParentSurface(),null));

		final ModelSurface restoreSurface=undoBuffer.remove(undoBuffer.size()-1);
		setSurfaceNoUndoCheck(model,restoreSurface);
		lastSurfaceState=restoreSurface.clone(false,restoreSurface.getResources().clone(),restoreSurface.getSchedules().clone(),restoreSurface.getParentSurface(),null);

		restoreSurface.setSelectedElement(null);
		fireSelectionListener();
		fireUndoRedoDoneListener();
	}

	/**
	 * Führt (wenn möglich) einen Wiederholen-Schritt aus
	 * @see #canUndo()
	 */
	public void doRedo() {
		if (redoBuffer.size()==0) return;

		undoBuffer.add(surface.clone(false,surface.getResources(),surface.getSchedules(),surface.getParentSurface(),null));

		final ModelSurface restoreSurface=redoBuffer.remove(redoBuffer.size()-1);
		setSurfaceNoUndoCheck(model,restoreSurface);
		lastSurfaceState=restoreSurface.clone(false,restoreSurface.getResources().clone(),restoreSurface.getSchedules().clone(),restoreSurface.getParentSurface(),null);

		restoreSurface.setSelectedElement(null);
		fireSelectionListener();
		fireUndoRedoDoneListener();
	}

	/**
	 * Zeigt einen Dialog zur Auswahl des Rückgängig- oder Wiederholen-Schritts an.
	 * @see #canUndo
	 * @see #canRedo()
	 */
	public void doUnDoRedoByDialog() {
		if (undoBuffer.size()==0 && redoBuffer.size()==0) return;

		final UndoRedoDialog dialog=new UndoRedoDialog(this,model,undoBuffer,surface,redoBuffer);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return;
		final int step=dialog.getSelectedStep();

		if (step>0) {
			/* Redo */
			for (int i=0;i<step;i++) doRedo();
			return;
		}

		if (step<0) {
			/* Undo */
			for (int i=0;i<-step;i++) doUndo();
		}
	}

	/**
	 * Stellt ein UserPaint-Objekt, welches beim Zeichnen zusätzlich aufgerufen wird
	 * @param additionalUserPaint	UserPaint-Objekt, das beim Zeichnen zusätzlich aufgerufen wird; kann <code>null</code> sein, wenn nichts zusätzlich aufgerufen werden soll.
	 */
	public void setAdditionalUserPaint(final UserPaint additionalUserPaint) {
		this.additionalUserPaint=additionalUserPaint;
	}

	/**
	 * Zeichnet die Surface-Elemente auf einem <code>Graphics</code>-Element ein. Hintergrund usw. werden dabei nicht gezeichnet.
	 * @param g	Grafik-Objekt auf dem gezeichnet werden soll
	 * @param viewArea	Darzustellender Bereich
	 * @param showBackground	Hintergrund anzeigen?
	 * @param showBoundingBox	Wenn Hintergrund aus: Wenigstens weißen Kasten? (bei svg-Export: aus, sonst: an)
	 * @param raster	Raster anzeigen?
	 * @param colors	2- oder 3-elementiges Array aus Hintergrund-, Raster- und optional oberer Gradienthintergrundfarbe
	 * @param backgroundImage	Optionales Hintergrundbild
	 * @param backgroundImageHash	Optionaler zusätzliches Hash-Wert des optionales Hintergrundbildes (siehe {@link ScaledImageCache#getHash(java.awt.Image)}
	 * @param backgroundImageScale	Skalierungsfaktor für das optionale Hintergrundbild
	 * @param showSelectionFrames	Rahmen anzeigen, wenn etwas ausgewählt ist
	 */
	public void paintElements(final Graphics g, final Rectangle viewArea, final ModelSurface.BackgroundImageMode showBackground, final boolean showBoundingBox, final ModelSurface.Grid raster, final Color[] colors, final BufferedImage backgroundImage, final String backgroundImageHash, final double backgroundImageScale, final boolean showSelectionFrames) {
		if (SetupData.getSetup().antialias) ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		/* Surface zeichnet alle Elemente in Graphics-Objekt */
		surface.drawToGraphics(g,viewArea,zoom,true,showBackground,showBoundingBox,raster,colors,backgroundImage,backgroundImageHash,backgroundImageScale,showSelectionFrames);
	}

	/**
	 * Stellt die vertikalen Positionen für die Infotexte,
	 * die sich auf die drei Schaltflächen in der vertikalen
	 * Leiste links neben der Zeichenfläche beziehen, ein.
	 * @param positions	Array aus drei Elementen mit den vertikalen Positionen für die Infotexte oder <code>null</code>, wenn keine Infotexte angezeigt werden sollen.
	 */
	public void setInfoPositions(final int[] positions) {
		if (positions==null || positions.length!=3) {
			infoPositions=null;
		} else {
			infoPositions=Arrays.copyOf(positions,3);
		}
	}

	/**
	 * Zeichenflächen-Links
	 * @see #paintInfo(Graphics, Rectangle)
	 */
	private ModelSurfaceLinks surfaceLinks;

	/**
	 * Zeichnet die zusätzlichen Infotexte auf der Zeichenfläche ein.
	 * @param g	Grafikausgabeobjekt
	 * @param viewArea	Sichtbarer Bereich
	 * @see #paint(Graphics)
	 */
	private void paintInfo(final Graphics g, final Rectangle viewArea) {
		if (infoPositions==null) {
			linkPositions=null;
			return;
		}

		/* Font-Einstellungen speichern */

		final Font saveFont=g.getFont();
		g.setFont(FontCache.getFontCache().getFont(FontCache.defaultFamily,0,(int)Math.round(12*GUITools.getScaleFactor())));

		/* Pfeile mit Beschriftungen für die Schaltflächen */

		g.setColor(Color.GRAY);
		((Graphics2D)g).setStroke(new BasicStroke(2));
		final int lineH=g.getFontMetrics().getAscent()+g.getFontMetrics().getDescent();
		final int lineYShift=g.getFontMetrics().getAscent();

		for (int i=0;i<3;i++) {
			final int y=infoPositions[i];
			g.drawLine(20,y,65,y);
			g.drawLine(20,y,30,y-10);
			g.drawLine(20,y,30,y+10);

			String text="";
			Image image=null;
			switch (i) {
			case 0:
				text=Language.tr("Editor.SurfaceTooltip.SurfaceInfoProperties");
				image=Images.MODEL.getImage();
				break;
			case 1:
				text=Language.tr("Editor.SurfaceTooltip.SurfaceInfoTemplates");
				image=Images.EDIT_ADD.getImage();
				break;
			case 2:
				text=Language.tr("Editor.SurfaceTooltip.SurfaceInfoAddEgde");
				image=Images.EDIT_EDGES_ADD.getImage();
				break;
			}

			if (image!=null) g.drawImage(image,70,y-8,null);

			final String[] lines=text.split("\\n");
			int textY=y-lineH*lines.length/2;
			for (String line: lines) {
				g.drawString(line,90,textY+lineYShift);
				textY+=lineH;
			}
		}

		/* Links */

		if (surfaceLinks==null) surfaceLinks=new ModelSurfaceLinks();
		linkPositions=surfaceLinks.drawLinks(g,20,infoPositions[2]+60);

		/* Font-Einstellungen zurücksetzen */

		g.setFont(saveFont);
	}

	@Override
	public void paint(Graphics g) {
		/* Kein Surface als Datenquelle eingetragen */
		if (surface==null) {super.paint(g); return;}

		/* Größe des Elements selbst einstellen */
		final int surfaceSizeX=(int)Math.round(SIZE*zoom);
		final int surfaceSizeY=(int)Math.round(SIZE*zoom);
		final Dimension currentSize=getPreferredSize();
		if (currentSize.width!=surfaceSizeX || currentSize.height!=surfaceSizeY) setPreferredSize(new Dimension(surfaceSizeX,surfaceSizeY));

		/* Bestimmung des tatsächlich dargestellten Ausschnitts */
		final Rectangle viewArea;
		if (getParent() instanceof JViewport) {
			final JViewport viewport=(JViewport)getParent();
			viewArea=viewport.getViewRect();
		} else {
			viewArea=new Rectangle(getSize());
		}

		/* Elemente zeichnen */
		paintElements(g,viewArea,backgroundImageMode,true,raster,colors,(backgroundImageMode!=ModelSurface.BackgroundImageMode.OFF)?backgroundImage:null,backgroundImageHash,backgroundImageScale,true);

		/* Ggf. Hinweis zu Steuerelementen einzeichnen */
		if (viewArea.x==0 && viewArea.y==0 && surface.count()==0 && useInfoPaint) {
			paintInfo(g,viewArea);
		} else {
			linkPositions=null;
		}

		/* Selektionsrahmen zeichnen */
		if (selectBox!=null) {
			g.setClip(viewArea.x,viewArea.y,viewArea.width,viewArea.height);
			g.setColor(new Color(0,0,255,12));
			g.fillRect(selectBox.x,selectBox.y,selectBox.width,selectBox.height);
			g.setColor(Color.BLUE);
			g.drawRect(selectBox.x,selectBox.y,selectBox.width,selectBox.height);
		}

		if (additionalUserPaint!=null) {
			g.setClip(viewArea.x,viewArea.y,viewArea.width,viewArea.height);
			additionalUserPaint.paint(g,zoom);
		}

		checkIfModelChanged(false);
	}

	/**
	 * Liefert die Zeichenfläche als Bilddatei.<br>
	 * Das Bild wird unter Beibehaltung des Seitenverhältnisses
	 * auf die angegebene Größe skaliert.
	 * @param xSize	Größe in x-Richtung
	 * @param ySize	Größe in y-Richtung
	 * @return	Bild-Objekt
	 */
	public BufferedImage getImage(final int xSize, final int ySize) {
		return getImageMaxFactor(xSize,ySize,-1,false);
	}

	/**
	 * Liefert die Zeichenfläche als Bilddatei.<br>
	 * Das Bild wird unter Beibehaltung des Seitenverhältnisses
	 * auf die angegebene Größe skaliert.
	 * @param xSize	Größe in x-Richtung
	 * @param ySize	Größe in y-Richtung
	 * @return	Bild-Objekt
	 */
	public BufferedImage getImageWithBackground(final int xSize, final int ySize) {
		return getImageMaxFactor(xSize,ySize,-1,true);
	}

	/**
	 * Liefert die Zeichenfläche als Bilddatei.<br>
	 * Das Bild wird nach Möglichkeit in Originalgröße ausgegeben
	 * und nötigenfalls unter Beibehaltung des Seitenverhältnisses
	 * auf die angegebene Größe verkleinert.
	 * @param xSizeMax	Größe in x-Richtung
	 * @param ySizeMax	Größe in y-Richtung
	 * @return	Bild-Objekt
	 */
	public BufferedImage getImageMaxSize(final int xSizeMax, final int ySizeMax) {
		return getImageMaxFactor(xSizeMax,ySizeMax,1,false);
	}

	/**
	 * Liefert die Zeichenfläche als Bilddatei.<br>
	 * Das Bild wird unter Beibehaltung des Seitenverhältnisses
	 * auf die angegebene Größe skaliert. Beim Skalieren nach oben
	 * wird jedoch der angegebene maximale Zoomfaktor nicht überschritten.
	 * @param xSize	Größe in x-Richtung (werden für die x- und die y-Größe Werte kleiner oder gleich 0 angegeben, so wird die tatsächliche Größe verwendet)
	 * @param ySize	Größe in y-Richtung (werden für die x- und die y-Größe Werte kleiner oder gleich 0 angegeben, so wird die tatsächliche Größe verwendet)
	 * @param maxZoomFactor	Maximaler Zoomfaktor (oder &lt;0, wenn keine Begrenzung nach oben erfolgen soll)
	 * @param withBackground	Soll der Hintergrund in dem Bild dargestellt werden?
	 * @return	Bild-Objekt
	 */
	public BufferedImage getImageMaxFactor(int xSize, int ySize, final double maxZoomFactor, final boolean withBackground) {
		final Point p1=surface.getUpperLeftModelCorner();
		final Point p2=surface.getLowerRightModelCorner();

		p1.x=Math.max(0,p1.x-2);
		p1.y=Math.max(0,p1.y-2);
		p2.x++;
		p2.y++;

		final int xDraw=p2.x-p1.x;
		final int yDraw=p2.y-p1.y;

		if (xSize<=0 && ySize<=0) {
			xSize=xDraw;
			ySize=yDraw;
		}

		double imageZoom=Math.min(xSize/((double)xDraw),ySize/((double)yDraw));
		if (maxZoomFactor>0 && imageZoom>maxZoomFactor) imageZoom=maxZoomFactor;

		final int xSurfaceImage=(int)Math.ceil(p2.x*imageZoom);
		final int ySurfaceImage=(int)Math.ceil(p2.y*imageZoom);

		BufferedImage image=new BufferedImage(xSurfaceImage,ySurfaceImage,BufferedImage.TYPE_INT_ARGB);
		Graphics g=image.getGraphics();
		g.setClip(0,0,xSurfaceImage,ySurfaceImage);
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
		surface.drawToGraphics(g,new Rectangle(0,0,xSurfaceImage,ySurfaceImage),imageZoom,withBackground,backgroundImageMode,true,ModelSurface.Grid.OFF,null,backgroundImage,backgroundImageHash,backgroundImageScale,false);
		if (additionalUserPaint!=null) {
			g.setClip(new Rectangle(0,0,xSurfaceImage,ySurfaceImage));
			additionalUserPaint.paint(g,imageZoom);
		}

		final int xRemove=(int)Math.round(p1.x*imageZoom);
		final int yRemove=(int)Math.round(p1.y*imageZoom);

		return image.getSubimage(xRemove,yRemove,xSurfaceImage-xRemove,ySurfaceImage-yRemove);
	}

	/**
	 * Liefert die Zeichenfläche als Bilddatei.<br>
	 * Das Bild wird unter Beibehaltung des Seitenverhältnisses
	 * ggf. auf die angegebene Größe hochskaliert.
	 * @param xSize	Größe in x-Richtung (werden für die x- und die y-Größe Werte kleiner oder gleich 0 angegeben, so wird die tatsächliche Größe verwendet)
	 * @param ySize	Größe in y-Richtung (werden für die x- und die y-Größe Werte kleiner oder gleich 0 angegeben, so wird die tatsächliche Größe verwendet)
	 * @return	Bild-Objekt
	 */
	public BufferedImage getImageMinSize(int xSize, int ySize) {
		final int MAX_IMAGE_BIGGER_FACTOR=10; /* Eingestellte Exportgröße maximal um diesen Faktor überschreiten */
		final int MAX_IMAGE_SIZE=15_000; /* Maximale Größe */

		final Point p1=surface.getUpperLeftModelCorner();
		final Point p2=surface.getLowerRightModelCorner();

		p1.x=Math.max(0,p1.x-2);
		p1.y=Math.max(0,p1.y-2);
		p2.x++;
		p2.y++;

		final int xDraw=p2.x-p1.x;
		final int yDraw=p2.y-p1.y;

		if (xSize<=0 && ySize<=0) {
			xSize=xDraw;
			ySize=yDraw;
		}

		double imageZoom=Math.max(xSize/((double)xDraw),ySize/((double)yDraw));

		int xSurfaceImage=(int)Math.ceil(p2.x*imageZoom);
		int ySurfaceImage=(int)Math.ceil(p2.y*imageZoom);

		if (xSurfaceImage>xSize*MAX_IMAGE_BIGGER_FACTOR) {
			ySurfaceImage=ySurfaceImage*xSize*MAX_IMAGE_BIGGER_FACTOR/xSurfaceImage;
			xSurfaceImage=xSize*MAX_IMAGE_BIGGER_FACTOR;
			imageZoom=((double)xSurfaceImage)/p2.x;
		}

		if (ySurfaceImage>ySize*MAX_IMAGE_BIGGER_FACTOR) {
			xSurfaceImage=xSurfaceImage*ySize*MAX_IMAGE_BIGGER_FACTOR/ySurfaceImage;
			ySurfaceImage=ySize*MAX_IMAGE_BIGGER_FACTOR;
			imageZoom=((double)ySurfaceImage)/p2.y;
		}

		if (xSurfaceImage>MAX_IMAGE_SIZE) {
			ySurfaceImage=ySurfaceImage*MAX_IMAGE_SIZE/xSurfaceImage;
			xSurfaceImage=MAX_IMAGE_SIZE;
			imageZoom=((double)xSurfaceImage)/p2.x;
		}

		if (ySurfaceImage>MAX_IMAGE_SIZE) {
			xSurfaceImage=xSurfaceImage*MAX_IMAGE_SIZE/ySurfaceImage;
			ySurfaceImage=MAX_IMAGE_SIZE;
			imageZoom=((double)ySurfaceImage)/p2.y;
		}

		BufferedImage image=new BufferedImage(xSurfaceImage,ySurfaceImage,BufferedImage.TYPE_INT_ARGB);
		Graphics g=image.getGraphics();
		g.setClip(0,0,xSurfaceImage,ySurfaceImage);
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);
		surface.drawToGraphics(g,new Rectangle(0,0,xSurfaceImage,ySurfaceImage),imageZoom,false,backgroundImageMode,true,ModelSurface.Grid.OFF,null,backgroundImage,backgroundImageHash,backgroundImageScale,false);
		if (additionalUserPaint!=null) {
			g.setClip(new Rectangle(0,0,xSurfaceImage,ySurfaceImage));
			additionalUserPaint.paint(g,imageZoom);
		}

		final int xRemove=(int)Math.round(p1.x*imageZoom);
		final int yRemove=(int)Math.round(p1.y*imageZoom);

		return image.getSubimage(xRemove,yRemove,xSurfaceImage-xRemove,ySurfaceImage-yRemove);
	}

	/**
	 * Speichert die Zeichenfläche als svg-Datei
	 * @param file	Dateiname, unter der die svg gespeichert werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich gespeichert werden konnte.
	 */
	public boolean saveSVGToFile(final File file) {
		int minX=Integer.MAX_VALUE;
		int minY=Integer.MAX_VALUE;
		int maxX=0;
		int maxY=0;
		for (ModelElement element: surface.getElements()) {
			final Point p1=element.getPosition(true);
			final Point p2=element.getLowerRightPosition();
			if (p1!=null) {minX=Math.min(minX,p1.x); minY=Math.min(minY,p1.y);}
			if (p2!=null) {maxX=Math.max(maxX,p2.x); maxY=Math.max(maxY,p2.y);}
		}

		final DOMImplementation domImpl=GenericDOMImplementation.getDOMImplementation();
		final Document document=domImpl.createDocument("http://www.w3.org/2000/svg","svg",null);
		final SVGGraphics2D svgGenerator=new SVGGraphics2D(document);
		svgGenerator.setSVGCanvasSize(new Dimension(maxX+minX,maxY+minY));

		final Rectangle area=new Rectangle(0,0,maxX*10,maxY*10);
		paintElements(svgGenerator,area,ModelSurface.BackgroundImageMode.OFF,false,ModelSurface.Grid.OFF,null,null,null,1.0,false);

		try (FileOutputStream fileWriter=new FileOutputStream(file)) {
			final Writer out=new OutputStreamWriter(fileWriter,StandardCharsets.UTF_8);
			svgGenerator.stream(out,true);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Speichert die Zeichenfläche als eps-Datei
	 * @param file	Dateiname, unter der die eps gespeichert werden soll
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich gespeichert werden konnte.
	 */
	public boolean saveEPSToFile(final File file) {
		int minX=Integer.MAX_VALUE;
		int minY=Integer.MAX_VALUE;
		int maxX=0;
		int maxY=0;
		for (ModelElement element: surface.getElements()) {
			final Point p1=element.getPosition(true);
			final Point p2=element.getLowerRightPosition();
			if (p1!=null) {minX=Math.min(minX,p1.x); minY=Math.min(minY,p1.y);}
			if (p2!=null) {maxX=Math.max(maxX,p2.x); maxY=Math.max(maxY,p2.y);}
		}

		final Graphics2D vg2d=new VectorGraphics2D() {
			@Override
			public void setColor(final Color color) {
				if (color!=null) setPaint(new GradientPaint(new Point(0,0),color,new Point(0,0),color));
				super.setColor(color);
			}
		};

		final Rectangle area=new Rectangle(0,0,maxX*10,maxY*10);
		paintElements(vg2d,area,ModelSurface.BackgroundImageMode.OFF,false,ModelSurface.Grid.OFF,null,null,null,1.0,false);

		final CommandSequence commands=((VectorGraphics2D)vg2d).getCommands();
		final EPSProcessor epsProcessor=new EPSProcessor();
		final de.erichseifert.vectorgraphics2d.Document doc=epsProcessor.getDocument(commands,new PageSize(maxX,maxY));
		try (FileOutputStream stream=new FileOutputStream(file)) {
			doc.writeTo(stream);
		} catch (IOException e) {
			return false;
		}

		return true;
	}

	/**
	 * Fügt bei einer DOCX-Ausgabe ein Bild des Modells in die Ausgabe ein.
	 * @param docx	DOCX-Ausgabe-Dokument
	 * @param xSize	Größe des Bildes in x-Richtung
	 * @param ySize	Größe des Bildes in y-Richtung
	 * @return	Liefert im Erfolgsfall <code>true</code>
	 */
	private boolean addImageToDOCX(final XWPFDocument docx, final int xSize, final int ySize) {
		final BufferedImage image=getImage(xSize,ySize);
		try (ByteArrayOutputStream streamOut=new ByteArrayOutputStream()) {
			try {if (!ImageIO.write(image,"png",streamOut)) return false;} catch (IOException e) {return false;}
			if (!XWPFDocumentPictureTools.addPicture(docx,streamOut,org.apache.poi.xwpf.usermodel.Document.PICTURE_TYPE_PNG,image.getWidth(),image.getHeight())) return false;
		} catch (IOException e) {return false;}
		return true;
	}

	/**
	 * Fügt bei einer DOCX-Ausgabe eine Überschrift in die Ausgabe ein.
	 * @param docx	DOCX-Ausgabe-Dokument
	 * @param line	Überschriftzeilen
	 */
	private void addHeadingToDOCX(final XWPFDocument docx, final String line) {
		final XWPFParagraph p=docx.createParagraph();
		final XWPFRun r=p.createRun();
		r.setBold(true);
		r.setFontSize(24);
		r.setText(line);
	}

	/**
	 * Speichert die Zeichenfläche als Bild in einer docx-Datei
	 * @param file	Dateiname, unter der die eps gespeichert werden soll
	 * @param xSize	Größe des eingebetteten Bilder in horizontaler Richtung
	 * @param ySize	Größe des eingebetteten Bilder in vertikaler Richtung
	 * @param addDescription	Soll nur das Bild ausgegeben werden (<code>false</code>) oder sollen auch Modellbeschreibung usw. ausgegeben werden (<code>true</code>)?
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich gespeichert werden konnte.
	 */
	public boolean saveDOCXToFile(final File file, final int xSize, final int ySize, final boolean addDescription) {
		try(XWPFDocument docx=new XWPFDocument()) {

			if (addDescription) {
				/* Überschrift */
				final String name;
				if (model.name.trim().isEmpty()) name=Language.tr("SlidesGenerator.Modell"); else name=model.name;
				addHeadingToDOCX(docx,name+"\n"+MainFrame.PROGRAM_NAME+" "+Language.tr("SlidesGenerator.ModelType"));
			}

			/* Bild */
			if (!addImageToDOCX(docx,xSize,ySize)) return false;

			if (addDescription) {
				/* Beschreibung */
				if (model.description!=null && !model.description.trim().isEmpty()) {
					final List<String> lines=new ArrayList<>(Arrays.asList(model.description.split("\\n")));
					final String[] content=lines.stream().filter(line->!line.trim().isEmpty()).toArray(String[]::new);
					if (content.length>0) {
						addHeadingToDOCX(docx,Language.tr("SlidesGenerator.ModellDescription"));
						final XWPFParagraph p=docx.createParagraph();
						for (String line: content) {
							p.createRun().setText(line);
							p.createRun().addBreak();
						}
					}
				}

				/* Parameter */
				addHeadingToDOCX(docx,Language.tr("SlidesGenerator.ModellParameters"));
				final ModelDescriptionBuilderStyled description=new ModelDescriptionBuilderStyled(model);
				description.run();
				description.saveDOCX(docx);
			}

			try (FileOutputStream out=new FileOutputStream(file)) {docx.write(out);}
			return true;
		} catch (IOException e) {return false;}
	}

	/**
	 * Speichert die Zeichenfläche als Bilddatei, svg- oder pdf-Datei
	 * @param file	Dateiname, unter der das Bild bzw. die pdf bzw. die svg-Datei gespeichert werden soll
	 * @param format	Dateiformat
	 * @param xSize	Größe des Bildes in x-Richtung
	 * @param ySize	Größe des Bildes in y-Richtung
	 * @return	Gibt <code>true</code> zurück, wenn das Bild erfolgreich gespeichert werden konnte.
	 */
	public boolean saveImageToFile(final File file, String format, final int xSize, final int ySize) {
		final int PDF_IMAGE_SIZE=2_000;
		final int MAX_IMAGE_BIGGER_FACTOR=10; /* Eingestellte Exportgröße maximal um diesen Faktor überschreiten */
		final int MAX_IMAGE_SIZE=10_000; /* Maximale Größe */

		if (format.equalsIgnoreCase("pdf")) {
			PDFWriter pdf=new PDFWriter(this,15,10);
			if (!pdf.systemOK) return false;
			if (!pdf.writeImage(getImage(PDF_IMAGE_SIZE,PDF_IMAGE_SIZE),25)) return false;
			return pdf.save(file);
		}

		if (format.equalsIgnoreCase("svg")) {
			return saveSVGToFile(file);
		}

		if (format.equalsIgnoreCase("eps")) {
			return saveEPSToFile(file);
		}

		if (format.equalsIgnoreCase("docx")) {
			return saveDOCXToFile(file,xSize,ySize,false);
		}

		/* gif, jpeg, png, bmp */
		final int maxXSize=Math.min(MAX_IMAGE_SIZE,MAX_IMAGE_BIGGER_FACTOR*xSize);
		final int maxYSize=Math.min(MAX_IMAGE_SIZE,MAX_IMAGE_BIGGER_FACTOR*ySize);
		BufferedImage image=getImageMaxFactor(maxXSize,maxYSize,10,false);
		if (format.equalsIgnoreCase("jpg")) format="jpeg";

		format=format.toLowerCase();

		if (format.equals("jpeg") || format.equals("bmp")) {
			final BufferedImage image2=new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_RGB);
			final Graphics2D g2=image2.createGraphics();
			g2.setBackground(Color.WHITE);
			g2.clearRect(0,0,image.getWidth(),image.getHeight());
			g2.drawImage(image,0,0,null);
			g2.dispose();
			image=image2;
		}

		try {return ImageIO.write(image,format,file);} catch (IOException e) {return false;}
	}

	/**
	 * Erzeugt einen Timer, der dafür sorgt, dass auch dann Tooltips für die Zeichenfläche
	 * angezeigt werden können, wenn diese durch ein anderes modales Fenster blockiert ist.
	 * @return	Timer zur Prüfung und Anzeige von Tooltips
	 */
	private Timer buildTooltipTimer() {
		final Window ownWindow=SwingUtilities.getWindowAncestor(ModelSurfacePanel.this);
		final Timer timer=new Timer("BackgroundTooltipUpdater");
		timer.scheduleAtFixedRate(new TimerTask() {
			private int lastX;
			private int lastY;
			@Override
			public void run() {
				final Point pScreen=MouseInfo.getPointerInfo().getLocation();
				if (lastX==pScreen.x && lastY==pScreen.y) return; /* Für Tooltips muss die Maus ruhig stehen, daher keine unnötigen Events senden. */
				lastX=pScreen.x;
				lastY=pScreen.y;
				final Point pLocal=new Point(pScreen);
				SwingUtilities.convertPointFromScreen(pLocal,ModelSurfacePanel.this);

				/* Keinen Tooltip anzeigen, wenn anderen Fenster an der aktuellen Mausposition ist */
				for (Window wnd: ownWindow.getOwnedWindows()) {
					if (!(wnd instanceof JDialog)) continue;
					if (!wnd.isDisplayable()) continue;
					if (!wnd.isVisible()) continue;
					final Point location=wnd.getLocationOnScreen();
					final Dimension size=wnd.getSize();
					if (pScreen.x>=location.x && pScreen.x<=location.x+size.width && pScreen.y>=location.y && pScreen.y<=location.y+size.height) return; /* Mauszeiger in anderem Fenster */
				}

				/* Mausbewegung an eigene Ereignisbehandlung durchreichen */
				final MouseEvent event=new MouseEvent(ModelSurfacePanel.this,MouseEvent.MOUSE_MOVED,System.currentTimeMillis(),0,pLocal.x,pLocal.y,pScreen.x,pScreen.y,0,false,0);
				ModelSurfacePanel.this.dispatchEvent(event);
			}
		},500,100);
		return timer;
	}

	/**
	 * Fokus-Listener, der beim Aufruf eines Element-Bearbeiten-Dialogs
	 * aktiviert und danach wieder deaktiviert wird. Auf diese Weise
	 * können der Tooltip-Timer deaktiviert und nach dem Dialog notwendige
	 * Updates durchgeführt werden.
	 * @see #showElementProperties(ModelElement, int)
	 */
	private FocusListener focusListener;

	/**
	 * Ruft den Modelleigenschaften-Dialog einer Station auf.
	 * @param element	Aktuelle Station
	 * @param modifieres	Gedrückte Umschalt-Tasten
	 */
	public void showElementProperties(final ModelElement element, final int modifieres) {
		if (element==null) return;
		surface.setSelectedArea(null,zoom);
		surface.setSelectedElement(element);
		fireSelectionListener();
		dragElement=null;
		dragCopyElement=null;
		dragStartElementPosition=null;
		element.setModel(model);

		final Timer timer=SetupData.getSetup().showBackgroundTooltips?buildTooltipTimer():null;

		focusListener=new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				fireStateChangeListener(); /* Die Aufrufe unten funktionieren nicht, da die Methode sofort zurückkehrt und die Dialog erst per invokeLater sichtbar werden. */
				removeFocusListener(focusListener);
				if (timer!=null) timer.cancel();
			}
		};
		addFocusListener(focusListener);

		/* Simulationsdaten */
		if ((modifieres & InputEvent.CTRL_DOWN_MASK)!=0 && (modifieres & InputEvent.SHIFT_DOWN_MASK)!=0 && (modifieres & InputEvent.ALT_DOWN_MASK)==0) {
			if (element.hasAnimationStatisticsData(simData)) {
				element.showElementAnimationStatisticsData(ModelSurfacePanel.this,simData);
				fireStateChangeListener();
				return;
			}
		}

		/* Untermodell bearbeiten */
		if ((modifieres & InputEvent.CTRL_DOWN_MASK)==0 && (modifieres & InputEvent.SHIFT_DOWN_MASK)!=0 && (modifieres & InputEvent.ALT_DOWN_MASK)==0) {
			showElementSubEditor(surface.getSelectedElement());
			fireStateChangeListener();
			return;
		}

		/* Elementeigenschaften bearbeiten */
		final Runnable propertiesRunner=element.getProperties(this,readOnly && !allowChangeOperationsOnReadOnly,clientData,sequences);
		if (propertiesRunner!=null) {
			propertiesRunner.run();
			fireStateChangeListener();
		}
	}

	/**
	 * Ruft den Dialog zum Bearbeiten eines Untermodells auf.
	 * @param element	Aktuelle Station
	 */
	private void showElementSubEditor(final ModelElement element) {
		if (!(element instanceof ModelElementSub)) return;
		surface.setSelectedArea(null,zoom);
		surface.setSelectedElement(element);
		fireSelectionListener();
		dragElement=null;
		dragCopyElement=null;
		dragStartElementPosition=null;
		((ModelElementSub)element).showSubEditDialog(this,readOnly && !allowChangeOperationsOnReadOnly,false);
		fireStateChangeListener();
	}

	/**
	 * Kopiert die ausgewählten Elemente in die Zwischenablage.
	 */
	public void copyToClipboard() {
		final Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		final ModelSurfaceTransferable transferable=new ModelSurfaceTransferable(surface.getTransferData(),surface.getTransferTxt(),surface.getTransferRtf(),surface.getTransferImage());
		clipboard.setContents(transferable,null);
	}

	/**
	 * Kopiert das gesamte Modell als Bild in die Zwischenablage
	 * (ggf. mit aktuellen Animationsdaten).
	 * @param xSize	Größe des Bildes in x-Richtung
	 * @param ySize	Größe des Bildes in y-Richtung
	 */
	public void copyToClipboardAsImage(final int xSize, final int ySize) {
		final int MAX_IMAGE_SIZE=10_000; /* Maximale Größe */

		final int maxXSize=Math.min(MAX_IMAGE_SIZE,xSize);
		final int maxYSize=Math.min(MAX_IMAGE_SIZE,ySize);
		final BufferedImage image=getImageMaxFactor(maxXSize,maxYSize,10,false);

		ImageTools.copyImageToClipboard(image);
	}

	/**
	 * Fügt die in der Zwischenablage befindlichen Elemente in die Zeichnung ein.
	 * Befinden sich keine Elemente in der Zwischenablage, dafür aber Text, so wird dieser
	 * als neues Text-Element eingefügt.
	 * @return	Gibt <code>true</code> zurück, wenn Elemente oder Text erfolgreich eingefügt werden konnten.
	 */
	public boolean pasteFromClipboard() {
		if (readOnly) return false;
		final Clipboard clipboard=Toolkit.getDefaultToolkit().getSystemClipboard();
		final Transferable transferable=clipboard.getContents(this);
		if (transferable==null) return false;

		/* Elemente einfügen */
		if (transferable.isDataFlavorSupported(ModelSurfaceTransferable.MODEL_FLAVOR)) {
			Object obj=null;
			try {obj=transferable.getTransferData(ModelSurfaceTransferable.MODEL_FLAVOR);} catch (UnsupportedFlavorException | IOException e) {obj=null;}
			if (obj instanceof ModelSurfaceTransferable) {
				clipboardData=((ModelSurfaceTransferable)obj).getStream();
				if (clipboardData==null) return false;
				setMode(ClickMode.MODE_INSERT_ELEMENTS_FROM_CLIPBOARD);
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				return true;
			}
		}

		/* Text als neues Element einfügen */
		if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			Object text=null;
			try {text=transferable.getTransferData(DataFlavor.stringFlavor);} catch (UnsupportedFlavorException | IOException e) {text=null;}
			if (text instanceof String) {
				clipboardText=(String)text;
				if (clipboardText.length()>1024) clipboardText=clipboardText.substring(0,1024);
				setMode(ClickMode.MODE_INSERT_TEXT);
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				return true;
			}
		}

		if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			Object image=null;
			try {image=transferable.getTransferData(DataFlavor.imageFlavor);} catch (UnsupportedFlavorException | IOException e) {image=null;}
			if (image instanceof BufferedImage) {
				clipboardImage=(BufferedImage)image;
				setMode(ClickMode.MODE_INSERT_IMAGE);
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}
		}

		if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			Object list=null;
			try {list=transferable.getTransferData(DataFlavor.javaFileListFlavor);} catch (UnsupportedFlavorException | IOException e) {list=null;}
			if (list instanceof List) for (Object entry: (List<?>)list) if (entry instanceof File) {
				dropFile((File)entry,null);
				break;
			}
		}

		return false;
	}

	/**
	 * Fügt die als Stream vorliegenden Elemente in die Zeichnung ein.
	 * @param data	Einzufügende Elemente
	 */
	public void pasteDirect(final ByteArrayInputStream data) {
		if (readOnly || data==null) return;
		clipboardData=data;
		setMode(ClickMode.MODE_INSERT_ELEMENTS_FROM_TEMPLATE);
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}

	/**
	 * Prüft, ob zwischen zwei Elementen eine Verbindungkanten eingefügt werden darf.
	 * @param element1	Mögliches Ausgangelement
	 * @param element2	Mögliches Zielelement
	 * @return	Liefert <code>true</code>, wenn zwischen den Elementen eine Verbindungskante eingefügt werden darf
	 */
	private boolean canAddEdge(final ModelElement element1, final ModelElement element2) {
		if (element1==null || element2==null) return false;
		if (element1==element2) return false;

		if (!(element1 instanceof ModelElementPosition)) return false;
		if (!(element2 instanceof ModelElementPosition)) return false;

		final ModelElementPosition position1=(ModelElementPosition)element1;
		final ModelElementPosition position2=(ModelElementPosition)element2;

		if (!position1.canAddEdgeOut()) return false;
		if (!position2.canAddEdgeIn()) return false;

		for (ModelElement element: surface.getElements()) if (element instanceof ModelElementEdge) {
			final ModelElementEdge edge=(ModelElementEdge)element;
			if (edge.getConnectionStart()==position1 && edge.getConnectionEnd()==position2) return false;
			if (edge.getConnectionStart()==position2 && edge.getConnectionEnd()==position1) return false;
		}

		return true;
	}

	/**
	 * Listener, die benachrichtigt werden sollen, wenn die Modelleigenschaften aufgerufen werden sollen
	 * @see #fireShowPropertiesDialog(String)
	 */
	private final List<ActionListener> showModelPropertiesListeners=new ArrayList<>();

	/**
	 * Es soll der Modelleigenschaftendialog angezeigt werden (wird als {@link ActionEvent#getActionCommand()} übergeben).
	 * @see #addShowModelPropertiesListener(ActionListener)
	 * @see #fireShowPropertiesDialog(String)
	 */
	public static String PROPERTIES_TYPE_PROPERTIES="showModelProperties";

	/**
	 * Es soll der Modelleigenschaftendialog mit der Ressourcen-Seite angezeigt werden (wird als {@link ActionEvent#getActionCommand()} übergeben).
	 * @see #addShowModelPropertiesListener(ActionListener)
	 * @see #fireShowPropertiesDialog(String)
	 */
	public static String PROPERTIES_TYPE_PROPERTIES_OPERATORS="showModelPropertiesResources";

	/**
	 * Es soll der Modelleigenschaftendialog mit der Transporter-Seite angezeigt werden (wird als {@link ActionEvent#getActionCommand()} übergeben).
	 * @see #addShowModelPropertiesListener(ActionListener)
	 * @see #fireShowPropertiesDialog(String)
	 */
	public static String PROPERTIES_TYPE_PROPERTIES_TRANSPORTERS="showModelPropertiesTransporters";

	/**
	 * Es soll der Modelleigenschaftendialog mit der Zeitpläne-Seite angezeigt werden (wird als {@link ActionEvent#getActionCommand()} übergeben).
	 * @see #addShowModelPropertiesListener(ActionListener)
	 * @see #fireShowPropertiesDialog(String)
	 */
	public static String PROPERTIES_TYPE_PROPERTIES_SCHEDULES="showModelPropertiesSchedules";

	/**
	 * Es soll der Dialog zur Konfiguration der Hintergrundfarbe angezeigt werden (wird als {@link ActionEvent#getActionCommand()} übergeben).
	 * @see #addShowModelPropertiesListener(ActionListener)
	 * @see #fireShowPropertiesDialog(String)
	 */
	public static String PROPERTIES_TYPE_BACKGROUND="showModelBackgroundColor";

	/**
	 * Es soll das Fenster zur Auswahl des Heatmap-Modus angezeigt werden (wird als {@link ActionEvent#getActionCommand()} übergeben).
	 * @see #addShowModelPropertiesListener(ActionListener)
	 * @see #fireShowPropertiesDialog(String)
	 */
	public static String PROPERTIES_TYPE_HEATMAP_MODES="showHeadMapModeSelect";

	/**
	 * Es soll der Dialog zur Konfiguration der Ebenen anzeigt werden (wird als {@link ActionEvent#getActionCommand()} übergeben).
	 * @see #addShowModelPropertiesListener(ActionListener)
	 * @see #fireShowPropertiesDialog(String)
	 */
	public static String PROPERTIES_TYPE_LAYERS="showLayers";

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn die Modelleigenschaften aufgerufen werden sollen
	 * @param showModelPropertiesListener	Zu benachrichtigender Listener
	 * @see #PROPERTIES_TYPE_PROPERTIES
	 * @see #PROPERTIES_TYPE_PROPERTIES_OPERATORS
	 * @see #PROPERTIES_TYPE_BACKGROUND
	 * @see #PROPERTIES_TYPE_HEATMAP_MODES
	 * @see #PROPERTIES_TYPE_LAYERS
	 */
	public void addShowModelPropertiesListener(final ActionListener showModelPropertiesListener) {
		if (showModelPropertiesListeners.indexOf(showModelPropertiesListener)<0) showModelPropertiesListeners.add(showModelPropertiesListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste, die benachrichtigt werden sollen, wenn die Modelleigenschaften aufgerufen werden sollen
	 * @param showModelPropertiesListener In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeShowModelPropertiesListener(final ActionListener showModelPropertiesListener) {
		return showModelPropertiesListeners.remove(showModelPropertiesListener);
	}

	/**
	 * Liste der Consumer, die benachrichtigt werden sollen, wenn die Anzahl an Bedienern in einer Gruppe geändert werden soll
	 * @see #setResourceCount(String, int)
	 */
	private final transient List<BiConsumer<String,Integer>> resourceCountSetter=new ArrayList<>();

	/**
	 * Fügt einen Consumer hinzu, der benachrichtigt werden soll, wenn die Anzahl an Bedienern in einer Gruppe geändert werden soll.
	 * @param setter	Consumer, der benachrichtigt werden soll, wenn die Anzahl an Bedienern in einer Gruppe geändert werden soll
	 */
	public void addResourceCountSetter(final BiConsumer<String,Integer> setter) {
		resourceCountSetter.add(setter);
	}

	/**
	 * Entfernt einen Consumer aus der Liste der Consumer hinzu, die benachrichtigt werden sollen, wenn die Anzahl an Bedienern in einer Gruppe geändert werden soll.
	 * @param setter	Consumer, der nicht mehr benachrichtigt werden soll, wenn die Anzahl an Bedienern in einer Gruppe geändert werden soll
	 * @return	Gibt <code>true</code> zurück, wenn der Consumer erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeResourceCountSetter(final BiConsumer<String,Integer> setter) {
		return resourceCountSetter.remove(setter);
	}

	/**
	 * Löst die Consumer, die benachrichtigt werden sollen, wenn die Anzahl an Bedienern in einer Gruppe geändert werden soll, aus.
	 * @param name	Name der Bedienergruppe
	 * @param count	Neue Anzahl an Bedienern in der Gruppe
	 */
	private void setResourceCount(final String name, final int count) {
		for (BiConsumer<String,Integer> setter: resourceCountSetter) setter.accept(name,count);
	}

	/**
	 * Fügt einen Menüpunkt zum Einfügen einer Visualisierung zu einem Menü hinzu
	 * @param parentMenu	Übergeordnetes Menü
	 * @param element	Visualisierungselement das durch den Menüpunkt eingefügt werden soll
	 * @return	Liefert den bereits eingefügten Menüpunkt zurück
	 */
	private JMenuItem addVisualizationContextMenuItem(final JMenu parentMenu, final ModelElementPosition element) {
		final JMenuItem item=new JMenuItem(element.getContextMenuElementName()+" - "+element.getName());
		item.setToolTipText(element.getToolTip());
		final Icon icon=element.getAddElementIcon();
		if (icon!=null) item.setIcon(icon);
		item.addActionListener(e->startAddElement(element));
		parentMenu.add(item);
		return item;
	}

	/**
	 * Fügt mehrere Visualisierungs-Kontextmenü-Einträge zu einem Menü hinzu
	 * @param parentMenu	Übergeordnetes Menü
	 */
	private void addVisualizationContextMenuItems(final JMenu parentMenu) {

		ModelElementAnimationTextValue text;
		ModelElementAnimationBar bar;
		ModelElementAnimationLineDiagram chart;
		List<Object[]> data;

		text=new ModelElementAnimationTextValue(null,null);
		text.setExpression("wip()");
		text.setName(Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal"));
		addVisualizationContextMenuItem(parentMenu,text);

		text=new ModelElementAnimationTextValue(null,null);
		text.setExpression("wip_avg()");
		text.setName(Language.tr("Surface.Popup.AddVisualization.AverageWIPTotal"));
		addVisualizationContextMenuItem(parentMenu,text);

		text=new ModelElementAnimationTextValue(null,null);
		text.setExpression("NQ()");
		text.setName(Language.tr("Surface.Popup.AddVisualization.CurrentNQTotal"));
		addVisualizationContextMenuItem(parentMenu,text);

		text=new ModelElementAnimationTextValue(null,null);
		text.setExpression("NQ_avg()");
		text.setName(Language.tr("Surface.Popup.AddVisualization.AverageNQTotal"));
		addVisualizationContextMenuItem(parentMenu,text);

		bar=new ModelElementAnimationBar(null,null);
		bar.getExpression().setExpression("wip()");
		bar.setSize(new Dimension(25,50));
		bar.setName(Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal"));
		addVisualizationContextMenuItem(parentMenu,bar);

		bar=new ModelElementAnimationBar(null,null);
		bar.getExpression().setExpression("wip_avg()");
		bar.setSize(new Dimension(25,50));
		bar.setName(Language.tr("Surface.Popup.AddVisualization.AverageWIPTotal"));
		addVisualizationContextMenuItem(parentMenu,bar);

		bar=new ModelElementAnimationBar(null,null);
		bar.getExpression().setExpression("NQ()");
		bar.setSize(new Dimension(25,50));
		bar.setName(Language.tr("Surface.Popup.AddVisualization.CurrentNQTotal"));
		addVisualizationContextMenuItem(parentMenu,bar);

		bar=new ModelElementAnimationBar(null,null);
		bar.getExpression().setExpression("NQ_avg()");
		bar.setSize(new Dimension(25,50));
		bar.setName(Language.tr("Surface.Popup.AddVisualization.AverageNQTotal"));
		addVisualizationContextMenuItem(parentMenu,bar);

		chart=new ModelElementAnimationLineDiagram(null,null);
		data=new ArrayList<>();
		data.add(new Object[]{new AnimationExpression("wip()"),Double.valueOf(0),Double.valueOf(10),Color.RED,Integer.valueOf(1)});
		data.add(new Object[]{"wip_avg()",Double.valueOf(0),Double.valueOf(10),Color.RED,Integer.valueOf(3)});
		chart.setExpressionData(data);
		chart.setTimeArea(3600);
		chart.setName(Language.tr("Surface.Popup.AddVisualization.CurrentWIPTotal"));
		chart.setSize(new Dimension(400,200));
		addVisualizationContextMenuItem(parentMenu,chart);

		chart=new ModelElementAnimationLineDiagram(null,null);
		data=new ArrayList<>();
		data.add(new Object[]{new AnimationExpression("NQ()"),Double.valueOf(0),Double.valueOf(10),Color.RED,Integer.valueOf(1)});
		data.add(new Object[]{new AnimationExpression("NQ_avg()"),Double.valueOf(0),Double.valueOf(10),Color.RED,Integer.valueOf(3)});
		chart.setExpressionData(data);
		chart.setTimeArea(3600);
		chart.setName(Language.tr("Surface.Popup.AddVisualization.CurrentNQTotal"));
		chart.setSize(new Dimension(400,200));
		addVisualizationContextMenuItem(parentMenu,chart);
	}

	/**
	 * Zeigt das Kontextmenü an einem bestimmten Punkt an.
	 * @param point	Klickpunkt an dem das Kontextmenü angezeigt werden soll
	 */
	private void showModelContextMenu(final Point point) {
		final JPopupMenu menu=new JPopupMenu();

		JMenu subMenu;
		JMenuItem item;

		if (!readOnly) {
			/* Slider für Bedieneranzahl */
			int groupCount=0;
			for (final ModelResource resource: model.resources.getResources()) {
				if (resource.getMode()!=ModelResource.Mode.MODE_NUMBER) continue;
				final String name=resource.getName();
				final Function<Integer,String> resourceNumberChange=value->{
					final int count;
					if (value==null) {
						count=resource.getCount();
					} else {
						count=value.intValue();
						setResourceCount(name,count);
					}
					return ""+count+" "+((count==1)?Language.tr("Editor.Operator.Singular"):Language.tr("Editor.Operator.Plural"));
				};
				menu.add(ModelElement.createContextMenuSliderValue(String.format(Language.tr("Editor.Operator"),resource.getName()),resource.getCount(),20,resourceNumberChange));
				groupCount++;
				if (groupCount==3) break;
			}
			if (groupCount>0) menu.addSeparator();

			/* Visualisierung hinzufügen */
			final JMenu sub=new JMenu(Language.tr("Surface.Popup.AddVisualization"));
			addVisualizationContextMenuItems(sub);
			if (sub.getItemCount()>0) {
				sub.setIcon(Images.MODELEDITOR_ELEMENT_ADD_VISUALIZATION.getIcon());
				menu.add(sub);
			}

			/* Notiz hinzufügen */
			menu.add(item=new JMenuItem(Language.tr("Surface.Popup.AddNote")));
			item.setIcon(Images.MODEL_NOTES.getIcon());
			item.addActionListener(e->startAddElement(new ModelElementNote(model,surface)));
		}

		/* Modelleigenschaften */
		menu.add(item=new JMenuItem(Language.tr("Main.Menu.File.ModelProperties")));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,InputEvent.CTRL_DOWN_MASK));
		item.setIcon(Images.MODEL.getIcon());
		item.addActionListener(e->fireShowPropertiesDialog(PROPERTIES_TYPE_PROPERTIES));

		/* Ebenen */
		menu.add(item=new JMenuItem(Language.tr("Main.Menu.View.Layers")));
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
		item.setIcon(Images.EDIT_LAYERS.getIcon());
		item.addActionListener(e->fireShowPropertiesDialog(PROPERTIES_TYPE_LAYERS));

		if (!readOnly) {
			final AnimationImageSource imageSource=new AnimationImageSource();

			/* Hintergrundfarbe */
			menu.add(item=new JMenuItem(Language.tr("Main.Menu.View.BackgroundColor")));
			item.setIcon(Images.EDIT_BACKGROUND_COLOR.getIcon());
			item.addActionListener(e->fireShowPropertiesDialog(PROPERTIES_TYPE_BACKGROUND));

			/* Heatmaptyp */
			menu.add(item=new JMenuItem(Language.tr("Main.Menu.View.Statistics.HeatMapSelect")));
			item.setAccelerator(KeyStroke.getKeyStroke('H',InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK));
			item.addActionListener(e->fireShowPropertiesDialog(PROPERTIES_TYPE_HEATMAP_MODES));

			/* Parameterreihe: Anzahl an Bedienern */
			subMenu=new JMenu(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeOperatorCount"));
			subMenu.setIcon(Images.PARAMETERSERIES.getIcon());
			for (ModelResource resource: model.resources.getResources()) {
				if (resource.getMode()==ModelResource.Mode.MODE_NUMBER && resource.getCount()>0) {
					String icon=resource.getIcon();
					if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_OPERATOR_ICON_NAME;
					subMenu.add(item=new JMenuItem(resource.getName(),new ImageIcon(imageSource.get(icon,model.animationImages,16))));
					item.addActionListener(e->{
						final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_OPERATORS,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeOperatorCount.Short"));
						record.input.setMode(ModelChanger.Mode.MODE_RESOURCE);
						record.input.setTag(resource.getName());
						fireBuildParameterSeries(record);
					});
				}
			}
			if (subMenu.getItemCount()>0) {
				if (subMenu.getItemCount()>1) {
					menu.add(subMenu);
				} else {
					item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeOperatorCount")+" - "+subMenu.getItem(0).getText(),Images.PARAMETERSERIES.getIcon());
					item.addActionListener(subMenu.getItem(0).getActionListeners()[0]);
					menu.add(item);
				}
			}

			/* Parameterreihe: Variablenwerte */
			subMenu=new JMenu(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeVariableValue"));
			subMenu.setIcon(Images.PARAMETERSERIES.getIcon());
			for (String name: model.getModelVariableNames()) {
				subMenu.add(item=new JMenuItem(name,Images.EXPRESSION_BUILDER_VARIABLE.getIcon()));
				item.addActionListener(e->{
					final TemplateRecord record=new TemplateRecord(TemplateMode.MODE_VARIABLES,Language.tr("Surface.PopupMenu.ParameterCompare.ChangeVariableValue.Short"));
					record.input.setMode(ModelChanger.Mode.MODE_VARIABLE);
					record.input.setTag(name);
					fireBuildParameterSeries(record);
				});
			}
			if (subMenu.getItemCount()>0) {
				if (subMenu.getItemCount()>1) {
					menu.add(subMenu);
				} else {
					item=new JMenuItem(Language.tr("Surface.PopupMenu.ParameterCompare.ChangeVariableValue")+" - "+subMenu.getItem(0).getText(),Images.PARAMETERSERIES.getIcon());
					item.addActionListener(subMenu.getItem(0).getActionListeners()[0]);
					menu.add(item);
				}
			}

		}

		menu.show(this,point.x,point.y);
	}

	/**
	 * Aktiviert die per {@link #addShowModelPropertiesListener(ActionListener)} registrierten Listener.
	 * @param propertiesType	Art des Eigenschaftendialogs der geöffnet werden soll
	 * @see #showModelPropertiesListeners
	 */
	public void fireShowPropertiesDialog(final String propertiesType) {
		final ActionEvent event=new ActionEvent(ModelSurfacePanel.this,AWTEvent.RESERVED_ID_MAX+1,propertiesType);
		for (ActionListener listener: showModelPropertiesListeners) listener.actionPerformed(event);
	}

	/**
	 * Sichtbarkeit der Elementenvorlagenleiste
	 * @author Alexander Herzog
	 * @see ModelSurfacePanel#addSetElementTemplatesVisibilityListener(Consumer)
	 */
	public enum ElementTemplatesVisibility {
		/** Elementenvorlagenleiste anzeigen */
		SHOW,
		/** Elementenvorlagenleiste ausblenden */
		HIDE,
		/** Sichtbarkeit der Elementenvorlagenleiste umschalten */
		TOGGLE
	}

	/**
	 * Listener, die benachrichtigt werden sollen, wenn die Elementenvorlagenleiste ein- oder ausgeblendet werden soll
	 * @see #setElementTemplatesVisibility(ElementTemplatesVisibility)
	 */
	private final List<Consumer<ElementTemplatesVisibility>> setElementTemplatesVisibilityListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn die Elementenvorlagenleiste ein- oder ausgeblendet werden soll
	 * @param setElementTemplatesVisibilityListener	Zu benachrichtigender Listener
	 */
	public void addSetElementTemplatesVisibilityListener(final Consumer<ElementTemplatesVisibility> setElementTemplatesVisibilityListener) {
		if (setElementTemplatesVisibilityListeners.indexOf(setElementTemplatesVisibilityListener)<0) setElementTemplatesVisibilityListeners.add(setElementTemplatesVisibilityListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste, die benachrichtigt werden sollen, wenn Elementenvorlagenleiste ein- oder ausgeblendet werden soll
	 * @param setElementTemplatesVisibilityListener In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeSetElementTemplatesVisibilityListener(final Consumer<ElementTemplatesVisibility> setElementTemplatesVisibilityListener) {
		return setElementTemplatesVisibilityListeners.remove(setElementTemplatesVisibilityListener);
	}

	/**
	 * Listener, die benachrichtigt werden sollen, wenn die Elementenvorlagenleiste ein- oder ausgeblendet werden soll
	 * @param visible	Soll die Elementenvorlagenleiste ein- oder ausgeblendet werden?
	 * @see #setElementTemplatesVisibilityListeners
	 */
	private void setElementTemplatesVisibility(final ElementTemplatesVisibility visible) {
		for (Consumer<ElementTemplatesVisibility> listener: setElementTemplatesVisibilityListeners) listener.accept(visible);
	}

	/**
	 * Bewegt das momentan ausgewählte Element um eine oder mehrere Stufen in der Zeichenflächen-Hierarchie nach vorne
	 * @param max	Wird hier <code>true</code> übergeben, so wird das Element ganz nach vorne verschoben; ansonsten nur um eine Stufe nach vorne
	 */
	public void moveSelectedElementToFront(final boolean max) {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surface.moveSelectedElementToFront(max);
	}

	/**
	 * Bewegt das momentan ausgewählte Element um eine oder mehrere Stufen in der Zeichenflächen-Hierarchie nach hinten
	 * @param max	Wird hier <code>true</code> übergeben, so wird das Element ganz nach hinten verschoben; ansonsten nur um eine Stufe nach hinten
	 */
	public void moveSelectedElementToBack(final boolean max) {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surface.moveSelectedElementToBack(max);
	}

	/**
	 * Richtet die ausgewählten Element an einer gemeinsamen Oberkante aus.
	 */
	public void alignSelectedElementsTop() {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surface.alignSelectedElementsTop();
	}

	/**
	 * Richtet die ausgewählten Element so aus, dass sich ihre vertikale Mitte auf derselben Höhe befindet.
	 */
	public void alignSelectedElementsMiddle() {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surface.alignSelectedElementsMiddle();
	}

	/**
	 * Richtet die ausgewählten Element an einer gemeinsamen Unterkante aus.
	 */
	public void alignSelectedElementsBottom() {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surface.alignSelectedElementsBottom();
	}

	/**
	 * Richtet die ausgewählten Element an einer gemeinsamen linken Kante aus.
	 */
	public void alignSelectedElementsLeft() {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surface.alignSelectedElementsLeft();
	}

	/**
	 * Richtet die ausgewählten Element so aus, dass sich ihre horizontale Mitte auf derselben Linie befindet.
	 */
	public void alignSelectedElementsCenter() {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surface.alignSelectedElementsCenter();
	}

	/**
	 * Richtet die ausgewählten Element an einer gemeinsamen rechten Kante aus.
	 */
	public void alignSelectedElementsRight() {
		if (readOnly && !allowChangeOperationsOnReadOnly) return;
		surface.alignSelectedElementsRight();
	}

	/**
	 * Gibt an, ob Kanten zu neu eingefügten Elementen wenn möglich automatisch hinzugefügt werden sollen.
	 * @return	Kanten-Hinzufüge-Modus
	 */
	public ConnectMode getAutoConnect() {
		return autoConnect;
	}

	/**
	 * Stellt ein, ob Kanten zu neu eingefügten Elementen wenn möglich automatisch hinzugefügt werden sollen.
	 * @param autoConnect	Kanten-Hinzufüge-Modus
	 */
	public void setAutoConnect(final ConnectMode autoConnect) {
		this.autoConnect=autoConnect;
	}

	/**
	 * Stellt die (Animations-)Simulationsdaten ein, so dass Statistikdaten während der Animation
	 * über das Kontextmenü abgerufen werden können.
	 * @param simData	Simulationsdaten (kann auch <code>null</code> sein, wenn keine Daten zur Verfügung gestellt werden sollen)
	 * @param mainAnimator	Animationssystem-Objekt (kann auch <code>null</code> sein, wenn keine Daten zur Verfügung gestellt werden sollen)
	 */
	public void setAnimationSimulationData(final SimulationData simData, final ModelSurfaceAnimator mainAnimator) {
		this.simData=simData;
		this.mainAnimator=mainAnimator;
	}

	/**
	 * Listener, die benachrichtigt werden sollen, wenn Elemente ausgewählt werden
	 * @see #fireSelectionListener()
	 */
	private transient List<ActionListener> selectionListeners=new ArrayList<>();

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
	 * Löst die Listener, die benachrichtigt werden sollen, wenn Elemente ausgewählt werden, aus.
	 * @see #selectionListeners
	 */
	private void fireSelectionListener() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"selectionchanged");
		for (ActionListener listener: selectionListeners) listener.actionPerformed(event);
	}

	/**
	 * Listener die auf Klicks auf Zeichenflächen-Links reagieren sollen
	 * @see #fireLinkListener(ui.modeleditor.ModelSurfaceLinks.Link)
	 */
	private transient List<Consumer<ModelSurfaceLinks.Link>> linkListeners=new ArrayList<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn ein Zeichenflächen-Link angeklickt wird
	 * @param linkListener	Zu benachrichtigender Listener
	 */

	public void addLinkListener(final Consumer<ModelSurfaceLinks.Link> linkListener) {
		if (linkListeners.indexOf(linkListener)<0) linkListeners.add(linkListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle eins Klicks auf einen Zeichenflächen-Link zu benachrichtigenden Listener
	 * @param linkListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeLinkListener(final Consumer<ModelSurfaceLinks.Link> linkListener) {
		return linkListeners.remove(linkListener);
	}

	/**
	 * Löst die Listener, die auf Klicks auf Zeichenflächen-Links reagieren sollen, aus.
	 * @param link	Nummer des Links
	 * @see #linkListeners
	 */
	private void fireLinkListener(final ModelSurfaceLinks.Link link) {
		for (Consumer<ModelSurfaceLinks.Link> linkListener: linkListeners) linkListener.accept(link);
	}

	/**
	 * Liefert das aktuelle selektierte Element
	 * @return	Aktuell selektiertes Element oder <code>null</code>, falls nichts selektiert ist.
	 */
	public ModelElement getSelectedElement() {
		return surface.getSelectedElement();
	}

	/**
	 * Liefert eine Liste der Elemente, die über eine Bereichsselektion markiert sind.
	 * @param addRegularSelectedElement	Auch die normal ausgewählten Objekte hinzufügen?
	 * @return	Liste der markierten Elemente (ist nie <code>null</code>, kann aber leer sein)
	 */
	public List<ModelElement> getSelectedArea(final boolean addRegularSelectedElement) {
		return surface.getSelectedArea(addRegularSelectedElement);
	}

	/**
	 * Liefert den Bereich der markierten Elemente.
	 * @return	Bereich der markierten Elemente oder <code>null</code>, wenn keine Elemente markiert sind.
	 */
	public Rectangle getSelectedArea() {
		int minX=Integer.MAX_VALUE;
		int minY=Integer.MAX_VALUE;
		int maxX=-Integer.MAX_VALUE;
		int maxY=-Integer.MAX_VALUE;

		for (ModelElement element: surface.getElements()) if ((element.isSelected() || element.isSelectedArea()) && (element instanceof ModelElementPosition)) {
			final ModelElementPosition pos=((ModelElementPosition)element);
			final Point p1=pos.getPosition(true);
			final Point p2=pos.getLowerRightPosition();
			if (p1.x<minX) minX=p1.x;
			if (p1.y<minY) minY=p1.y;
			if (p2.x>maxX) maxX=p2.x;
			if (p2.y>maxY) maxY=p2.y;
		}

		if (minX==Integer.MAX_VALUE) return null;

		return new Rectangle(minX,minY,maxX-minX,maxY-minY);
	}

	/**
	 * Liefert die momentane Hintergrund- und Rasterfarbe
	 * @return	2-elementiges Array aus Hintergrund- und Rasterfarbe
	 */
	public Color[] getColors() {
		return colors;
	}

	/**
	 * Stellt die Hintergrund- und Rasterfarbe ein
	 * @param colors	2-elementiges Array aus neuer Hintergrund- und Rasterfarbe
	 */
	public void setColors(final Color[] colors) {
		if (colors==null || colors.length<2 || colors[0]==null || colors[1]==null) return;
		if (colors.length==2 || colors[2]==null) {
			this.colors=Arrays.copyOf(colors,2);
		} else {
			this.colors=Arrays.copyOf(colors,3);
		}
		repaint();
	}

	/**
	 * Stellt das aktuelle Hintergrundbild ein.
	 * @param image	Hintergrundbild (kann <code>null</code> sein)
	 * @param scale	Skalierung für das Hintergrundbild (muss größer als 0 sein)
	 * @param mode	Soll das Hintergrundbild, sofern der <code>image</code>-Parameter ungleich <code>null</code> ist, überhaupt gezeichnet werden?
	 */
	public void setBackgroundImage(final BufferedImage image, final double scale, final ModelSurface.BackgroundImageMode mode) {
		if (image==null) {
			backgroundImage=null;
			backgroundImageHash=null;
		} else {
			backgroundImage=ScaledImageCache.copyImage(image);
			backgroundImageHash=ScaledImageCache.getHash(backgroundImage);
		}
		backgroundImageMode=mode;
		backgroundImageScale=Math.max(0.1,scale);
	}

	/**
	 * Liefert das aktuell eingestellte Hintergrundbild.
	 * @return	Aktuell eingestelltes Hintergrundbild (kann <code>null</code> sein)
	 * @see #setBackgroundImage(BufferedImage, double, ModelSurface.BackgroundImageMode)
	 * @see #getBackgroundImageScale()
	 */
	public BufferedImage getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * Liefert den Hash-Wert des aktuellen Hintergrundbildes
	 * (kann <code>null</code> sein, wenn kein Hintergrundbild eingestellt ist)
	 * @return	Hash-Wert des aktuellen Hintergrundbildes
	 * @see #setBackgroundImage(BufferedImage, double, ModelSurface.BackgroundImageMode)
	 */
	public String getBackgroundImageHash() {
		return backgroundImageHash;
	}

	/**
	 * Liefert die aktuelle Skalierung für das Hintergrundbild.
	 * @return	Aktuelle Skalierung für das Hintergrundbild
	 * @see #setBackgroundImage(BufferedImage, double, ModelSurface.BackgroundImageMode)
	 * @see #getBackgroundImage()
	 */
	public double getBackgroundImageScale() {
		return backgroundImageScale;
	}

	/**
	 * Liefert die Einstellung, wie das Hintergrundbild gezeichnet werden soll.
	 * @return	Wie soll das Hintergrundbild gezeichnet werden?
	 * @see ModelSurface.BackgroundImageMode
	 */
	public ModelSurface.BackgroundImageMode getBackgroundImageMode() {
		return backgroundImageMode;
	}

	/**
	 * Fügt eine Tabellen-basierte Kundenquelle zu dem Modell hinzu
	 * @param file	Tabellendatei
	 * @param position	Einfügeposition
	 * @return	Liefert true, wenn die Quelle erfolgreich hinzugefügt werden konnte
	 */
	private boolean addTableFileBasedSource(final File file, final Point position) {
		final ModelElementSourceTable source=new ModelElementSourceTable(model,surface);
		source.setInputFile(file.toString());
		source.setPosition(new Point((int)Math.round(position.x/zoom),(int)Math.round(position.y/zoom)));
		surface.add(source);
		return true;
	}

	/**
	 * Fügt eine Tabellen-basierte Eingabe-Station zu dem Modell hinzu
	 * @param file	Tabellendatei
	 * @param position	Einfügeposition
	 * @return	Liefert true, wenn die Eingabe-Station erfolgreich hinzugefügt werden konnte
	 */
	private boolean addTableFileBasedInput(final File file, final Point position) {
		final ModelElementInput input=new ModelElementInput(model,surface);
		input.setInputFile(file.toString());
		input.setPosition(new Point((int)Math.round(position.x/zoom),(int)Math.round(position.y/zoom)));
		surface.add(input);
		return true;
	}

	/**
	 * Erzeugt eine Tabellen-Quelle oder ein Eingabeelement mit der angegebenen Datei als Quelle an der angegebenen Position
	 * @param file	Datei, die als Datenquelle für das Element verwendet werden soll
	 * @param position	Maus-Position bezogen auf dieses Objekt (d.h. nicht Zeichenflächen-Punkt)
	 * @return	Gibt <code>true</code> zurück, wenn das Tabellen-basierende-Element hinzugefügt werden konnte
	 */
	public boolean addTableFileBasedElement(final File file, final Point position) {
		final int select=MsgBox.options(this,Language.tr("Editor.DragDropTableLoad.Title"),Language.tr("Editor.DragDropTableLoad.Info"),new String[]{Language.tr("Editor.DragDropTableLoad.OptionSource.Title"),Language.tr("Editor.DragDropTableLoad.OptionInput.Title")},new String[]{Language.tr("Editor.DragDropTableLoad.OptionSource.Info"),Language.tr("Editor.DragDropTableLoad.OptionInput.Info")});

		switch (select) {
		case 0: return addTableFileBasedSource(file,position);
		case 1: return addTableFileBasedInput(file,position);
		default: return false;
		}
	}

	/**
	 * Erzeugt ein Bild-Element auf der Zeichenfläche
	 * @param image	Bild, das in dem Bild-Element angezeigt werden soll
	 * @param position	Maus-Position bezogen auf dieses Objekt (d.h. nicht Zeichenflächen-Punkt)
	 * @return	Gibt <code>true</code> zurück, wenn das Bild-Element hinzugefügt werden konnte
	 */
	public boolean addImageElement(final BufferedImage image, final Point position) {
		final ModelElementImage element=new ModelElementImage(model,surface);
		element.setImage(image);
		element.setPosition(new Point((int)Math.round(position.x/zoom),(int)Math.round(position.y/zoom)));
		surface.add(element);
		return true;
	}

	/**
	 * Befindet sich ein Punkt im dem Bereich eines Infotext-Links?
	 * @param point	Zu prüfender Punkt
	 * @return	Liefert den Infotext-Link oder <code>null</code>, wenn sich der Punkt nicht im Bereich eines Infotextes befindet
	 * @see #linkPositions
	 */
	private ModelSurfaceLinks.Link emptyScreenLinkPosition(final Point point) {
		final Rectangle viewArea;
		if (getParent() instanceof JViewport) {
			final JViewport viewport=(JViewport)getParent();
			viewArea=viewport.getViewRect();
		} else {
			viewArea=new Rectangle(getSize());
		}

		if (viewArea.x==0 && viewArea.y==0 && surface.count()==0 && useInfoPaint && linkPositions!=null) {
			for (int i=0;i<linkPositions.length;i++) if (linkPositions[i]!=null && linkPositions[i].contains(point)) {
				if (surfaceLinks==null) return null;
				return surfaceLinks.getLink(i);
			}
		}

		return null;
	}

	/**
	 * Zeigt das QuickFix-Popupmenü für ein Element an (wenn es Fehler gibt, sonst passiert nichts)
	 * @param element	Element für dass das QuickFix-Popupmenü angezeigt werden soll
	 */
	private void showQuickFixPopup(final ModelElementPosition element) {
		final JPopupMenu popupMenu=element.getQuickFixPopupMenu();
		if (popupMenu==null) return;

		final Point point=element.getMiddlePosition(true);
		popupMenu.show(this,(int)Math.round(point.x*zoom),(int)Math.round(point.y*zoom));
	}

	/**
	 * Schaltet einige Funktionen zum Bearbeiten auch während des Read-Only-Modus frei.<br>
	 * Erlaubt wird das Bearbeiten von Elementen sowie das Verschieben.
	 * Das Löschen von Elementen und das Erstellen neuer Elemente bleibt aber verboten.
	 */
	public void allowEditorDialogs() {
		allowChangeOperationsOnReadOnly=true;
	}

	/**
	 * Zeigt an, ob gerade eine Operation, die evtl. mehrere Elemente umfasst, läuft und daher Statusausgaben usw. zunächst nicht verarbeitet werden sollen.
	 * Alle Funktionen, die hier zu einer Rückgabe von <code>true</code> führen, stellen sicher, dass nach Abschluss der Operation
	 * noch eine weitere Benachrichtigung mit bei Rückgabewert <code>false</code> hier ausgeführt wird.
	 * @return	Gibt <code>true</code> zurück, wenn gerade eine umfangreichere Operation ausgeführt wird.
	 */
	public boolean isOperationRunning() {
		return operationRunning;
	}

	/**
	 * Liefert eine Beschreibung für eine Station zur Anzeige in einem Tooltip.
	 * @param element	Element für das die Beschreibung generiert werden soll
	 * @return	Beschreibung
	 */
	private String getTooltipDescription(final ModelElementBox element) {
		if (element==null || !SetupData.getSetup().showStationDescription) return "";

		final ModelDescriptionBuilderSingleStation simpleDescriptionBuilder=new ModelDescriptionBuilderSingleStation(model);
		element.buildDescription(simpleDescriptionBuilder);
		simpleDescriptionBuilder.done();
		return simpleDescriptionBuilder.getDescription();
	}

	/**
	 * Liefert, sofern hinterlegt, einen zusätzlichen Tooltip-Text für ein Element
	 * @param element	Element für das der zusätzliche Tooltip-Text abgerufen werden soll
	 * @return	Zusätzlicher Tooltip-Text (kann <code>null</code> sein, wenn kein zusätzlicher Text zur Verfügung steht)
	 * @see #additionalTooltipGetter
	 * @see #setAdditionalTooltipGetter(Function)
	 */
	private String getAdditionalTooltip(final ModelElementBox element) {
		if (additionalTooltipGetter==null) return null;
		return additionalTooltipGetter.apply(element);
	}

	/**
	 * Stellt ein Callback ein, das zu einem Element weitere Tooltip-Daten (aus der Statistik) liefert
	 * @param additionalTooltipGetter	Callback, das weitere Tooltip-Daten liefert. (Dass Callback kann <code>null</code> sein und auch die zurückgelieferten Tooltip-Daten können jederzeit <code>null</code> sein)
	 */
	public void setAdditionalTooltipGetter(final Function<ModelElementBox,String> additionalTooltipGetter) {
		this.additionalTooltipGetter=additionalTooltipGetter;
	}

	/**
	 * Callback zur Ermittlung der HeatMap-Intensität für ein Element
	 * (kann <code>null</code> sein)
	 */
	private Function<ModelElementBox,Double> heatMapIntensityGetter;

	/**
	 * Stellt das Callback zur Ermittlung der HeatMap-Intensität für ein Element ein.
	 * @param heatMapIntensityGetter	Callback zur Ermittlung der HeatMap-Intensität für ein Element (<code>null</code>, wenn keine HeatMap gezeichnet werden soll)
	 */
	public void setHeatMapIntensityGetter(final Function<ModelElementBox,Double> heatMapIntensityGetter) {
		this.heatMapIntensityGetter=heatMapIntensityGetter;
		if (surface!=null) surface.setHeatMapIntensityGetter(heatMapIntensityGetter);
	}

	/**
	 * Löscht das aktuelle Heatmap-Bild,
	 * so dass die Heatmap beim nächsten Zeichnen neu angelegt wird.<br>
	 * Dies ist nötig, wenn die Heatmap-Einstellungen verändert wurden.
	 */
	public void clearHeatMapCache() {
		if (surface!=null) surface.clearHeatMapCache();
	}

	/**
	 * Listener, die benachrichtigt werden sollen, wenn der Nutzer per Kontextmenü die Erstellung einer Parameterreihe auslöst
	 * @see #fireBuildParameterSeries(ui.parameterseries.ParameterCompareTemplatesDialog.TemplateRecord)
	 */
	private final transient Set<Consumer<ParameterCompareTemplatesDialog.TemplateRecord>> buildParameterSeriesListeners=new HashSet<>();

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
	 * Löst eine Benachrichtigung der Listener, die benachrichtigt werden sollen, wenn der Nutzer per Kontextmenü die Erstellung einer Parameterreihe auslöst, aus.
	 * @param template	Vorlage für die Parameterreihe
	 */
	private void fireBuildParameterSeries(final ParameterCompareTemplatesDialog.TemplateRecord template) {
		buildParameterSeriesListeners.stream().forEach(listener->listener.accept(template));
	}

	/**
	 * Reagiert auf Mausklicks auf der Zeichenfläche
	 */
	private class ModelSurfacePanelMouseListener implements MouseListener {
		@Override
		public void mouseClicked(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
			requestFocus();
			requestFocusInWindow();

			/* Linksklick: Drag-Start / Auswahl */
			if (mode==ClickMode.MODE_NORMAL && SwingUtilities.isLeftMouseButton(e)) {
				dragStartMousePosition=e.getPoint();
				if (surface==null) return;
				ModelElement element=surface.getElementAtPosition(e.getPoint(),zoom);
				if (element!=null && !surface.isVisibleOnLayer(element)) element=null;
				if (element==null) {
					final ModelSurfaceLinks.Link link=emptyScreenLinkPosition(e.getPoint());
					if (link!=null) {
						fireLinkListener(link);
					} else {
						operationRunning=true;
						try {
							surface.setSelectedArea(null,zoom);
							surface.setSelectedElement(null);
						} finally {
							operationRunning=false;
							fireSelectionListener();
						}
						dragElement=null;
					}
				} else {
					if (e.isShiftDown() && surface.getSelectedElement()!=null && surface.getSelectedElement()!=element) {
						surface.getSelectedElement().setSelectedArea(true);
						surface.setSelectedElement(element);
						fireSelectionListener();
						dragElement=element;
					} else {
						dragElement=element;
						if (!readOnly || allowChangeOperationsOnReadOnly) dragStartElementBorderPointNr=element.getBorderPointNr(new Point((int)Math.round(e.getPoint().x/zoom),(int)Math.round(e.getPoint().y/zoom)));

						if (!e.isShiftDown() && !element.isSelected() && !element.isSelectedArea()) {
							surface.setSelectedArea(null,zoom);
						}
						if (!e.isShiftDown() && !element.isSelected() && element.isSelectedArea()) {
							if (getSelectedElement()!=null && !getSelectedElement().isSelectedArea()) getSelectedElement().setSelectedArea(true);
						}

						surface.setSelectedElement(dragElement);
						fireSelectionListener();
						if (!readOnly || allowChangeOperationsOnReadOnly) {
							if (dragStartElementBorderPointNr>=0) {
								dragStartElementPosition=dragElement.getBorderPointPosition(dragStartElementBorderPointNr);
							} else {
								dragStartElementPosition=dragElement.getPosition(false);
							}
						}
					}
				}
				fireStateChangeListener();
				/* hier kein return, sonst funktioniert Doppelklick (s.u.) nicht mehr. */
			}

			/*
			 *  Manchmal ist ein Klick zum Einfügen eines Elements auf die Zeichenfläche ein Doppelklick,
			 *  daher erst versuchen als Einfüge-Klick zu interpretieren und dann erst Doppelklick-Prüfung.
			 */

			/* Linksklick: Element hinzufügen */
			if (SwingUtilities.isLeftMouseButton(e) && mode==ClickMode.MODE_ADD_ELEMENT && modeAddElement!=null) {
				if (surface==null || readOnly) return;
				final ModelElement element=modeAddElement.clone(model,surface);
				element.setPosition(new Point((int)Math.round(e.getX()/zoom),(int)Math.round(e.getY()/zoom)));
				surface.add(element);
				if (modeAddElementSource!=null) {
					if (modeAddElementSource.canAddEdgeOut() && (element instanceof ModelElementBox) && ((ModelElementBox)element).canAddEdgeIn()) {
						final ModelElementEdge edge=new ModelElementEdge(model,surface,modeAddElementSource,element);
						modeAddElementSource.addEdgeOut(edge);
						((ModelElementBox)element).addEdgeIn(edge);
						surface.add(edge);
					}
					modeAddElementSource=null;
				}
				setMode(ClickMode.MODE_NORMAL);
				fireStateChangeListener();
				e.consume();
				return;
			}

			/* Doppelklick: Element bearbeiten */
			if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount()==2) {
				setMode(ClickMode.MODE_NORMAL);
				if (surface!=null) showElementProperties(surface.getElementAtPosition(e.getPoint(),zoom),e.getModifiersEx());
				e.consume();
				return;
			}

			/* Linksklick: Erstes Element für Kante gewählt */
			if (SwingUtilities.isLeftMouseButton(e) && mode==ClickMode.MODE_ADD_EDGE_STEP1) {
				if (surface==null || readOnly) return;
				final ModelElement element=surface.getElementAtPosition(e.getPoint(),zoom);
				boolean ok=false;
				if (element instanceof ModelElementPosition) ok=((ModelElementPosition)element).canAddEdgeOut();
				if (ok) {
					modeAddElement=element;
					setMode(ClickMode.MODE_ADD_EDGE_STEP2);
				}
				e.consume();
				return;
			}

			/* Linksklick oder mittlere Maustaste: Zweites Element für Kante gewählt -> Kante hinzufügen */
			if ((SwingUtilities.isLeftMouseButton(e) || SwingUtilities.isMiddleMouseButton(e)) && mode==ClickMode.MODE_ADD_EDGE_STEP2 && modeAddElement!=null) {
				if (surface==null || readOnly) return;
				final ModelElement element=surface.getElementAtPosition(e.getPoint(),zoom);
				if (canAddEdge(modeAddElement,element)) {
					ModelElementEdge edge=new ModelElementEdge(model,surface,modeAddElement,element);
					if (((ModelElementPosition)modeAddElement).addEdgeOut(edge)) {
						if (((ModelElementPosition)element).addEdgeIn(edge)) {
							surface.add(edge);
						} else {
							((ModelElementPosition)modeAddElement).removeConnectionNotify(edge);
						}
					}
					setMode(ClickMode.MODE_ADD_EDGE_STEP1);
					checkIfModelChanged(true);
					fireStateChangeListener();
				}
				e.consume();
				return;
			}

			/* Mittlere Maustaste: Erstes Element für Kante wählen */
			if (SwingUtilities.isMiddleMouseButton(e) && (mode==ClickMode.MODE_NORMAL || mode==ClickMode.MODE_ADD_EDGE_STEP1)) {
				if (surface==null || readOnly) return;
				final ModelElement element=surface.getElementAtPosition(e.getPoint(),zoom);
				boolean ok=false;
				if (element instanceof ModelElementPosition) ok=((ModelElementPosition)element).canAddEdgeOut();
				if (ok) {
					modeAddElement=element;
					setMode(ClickMode.MODE_ADD_EDGE_STEP2);
				} else {
					switch (mode) {
					case MODE_NORMAL: setMode(ClickMode.MODE_ADD_EDGE_STEP1); break;
					case MODE_ADD_EDGE_STEP1: setMode(ClickMode.MODE_NORMAL); break;
					default: /* Tritt nicht auf, ob in der if-Anweisung werden nur die beiden anderen case-Fälle überhaupt zugelassen. */ break;
					}
				}
				e.consume();
				return;
			}

			/* Linksklick: Elemente aus Zwischenablage einfügen */
			if (SwingUtilities.isLeftMouseButton(e) && (mode==ClickMode.MODE_INSERT_ELEMENTS_FROM_CLIPBOARD || mode==ClickMode.MODE_INSERT_ELEMENTS_FROM_TEMPLATE) && clipboardData!=null) {
				surface.setSelectedElement(null);
				surface.setSelectedArea(null,zoom);
				surface.setTransferData(clipboardData,e.getPoint(),zoom);
				clipboardData=null;
				setMode(ClickMode.MODE_NORMAL);
				fireStateChangeListener();
				e.consume();
				return;
			}

			/* Linksklick: Text aus Zwischenablage einfügen */
			if (SwingUtilities.isLeftMouseButton(e) && mode==ClickMode.MODE_INSERT_TEXT && clipboardText!=null) {
				surface.setSelectedElement(null);
				surface.setSelectedArea(null,zoom);
				final ModelElementText text=new ModelElementText(model,surface);
				text.setText(clipboardText);
				text.setPosition(new Point((int)Math.round(e.getPoint().x/zoom),(int)Math.round(e.getPoint().y/zoom)));
				surface.add(text);
				clipboardText=null;
				setMode(ClickMode.MODE_NORMAL);
				fireStateChangeListener();
				e.consume();
				return;
			}

			/* Linksklick: Bild aus Zwischenablage einfügen */
			if (SwingUtilities.isLeftMouseButton(e) && mode==ClickMode.MODE_INSERT_IMAGE && clipboardImage!=null) {
				surface.setSelectedElement(null);
				surface.setSelectedArea(null,zoom);
				final ModelElementImage image=new ModelElementImage(model,surface);
				image.setImage(clipboardImage);
				image.setPosition(new Point((int)Math.round(e.getPoint().x/zoom),(int)Math.round(e.getPoint().y/zoom)));
				surface.add(image);
				clipboardImage=null;
				setMode(ClickMode.MODE_NORMAL);
				fireStateChangeListener();
				e.consume();
				return;
			}

			/* Rechtsklick: Kontextmenü */
			if (SwingUtilities.isRightMouseButton(e)) {
				setMode(ClickMode.MODE_NORMAL);
				if (surface==null) return;
				setToolTipText(null);
				final ModelElement element=surface.getElementAtPosition(e.getPoint(),zoom);
				surface.setSelectedArea(null,zoom);
				if (element==null) {
					if (showEditModelProperties) showModelContextMenu(e.getPoint());
				} else {
					surface.setSelectedElement(element);
					fireStateChangeListener();
					element.setModel(model);
					element.showContextMenu(ModelSurfacePanel.this,e.getPoint(),readOnly,allowChangeOperationsOnReadOnly,(e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK)==InputEvent.SHIFT_DOWN_MASK,ModelSurfacePanel.this,template->fireBuildParameterSeries(template),clientData,sequences,simData,mainAnimator);
				}
				fireSelectionListener();
				e.consume();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			setCursor(Cursor.getDefaultCursor());
			if (mode==ClickMode.MODE_NORMAL && SwingUtilities.isLeftMouseButton(e)) {
				if (!e.isShiftDown() && dragStartMousePosition!=null && Math.abs(dragStartMousePosition.x-e.getX())+Math.abs(dragStartMousePosition.y-e.getY())==0) {
					operationRunning=true;
					try {
						surface.setSelectedArea(null,1.0);
					} finally {
						operationRunning=false;
						fireSelectionListener();
					}
					final ModelElement element=surface.getElementAtPosition(e.getPoint(),zoom);
					if (element!=null) {
						element.setSelected(true);
						if (element instanceof ElementAnimationClickable) {
							final Point click=e.getPoint();
							final Point vertex=element.getPosition(true);
							final int x=(int)Math.round(click.x/zoom)-vertex.x;
							final int y=(int)Math.round(click.y/zoom)-vertex.y;
							((ElementAnimationClickable)element).clicked(x,y);
						}
					}
				}

				if (dragElement!=null && dragCopyElement!=null) {
					surface.setSelectedArea(null,zoom);
					surface.smartRename(dragCopyElement);
					surface.setSelectedElement(dragCopyElement);
				}
				if (selectBox!=null) {
					surface.setSelectedArea(selectBox,zoom);
					fireStateChangeListener();
				}
				fireSelectionListener();
				dragStartMousePosition=null;
				dragStartElementBorderPointNr=-1;
				dragStartElementPosition=null;
				dragCopyElement=null;
				selectBox=null;
				repaint();
			}

			dragElement=null;
			fireStateChangeListener(); /* Weil wir dies während des Drag&Drop explizit unterdrücken. */
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			/*
			Würde die QuickAccess-Eingabezeile ganz erheblich stören:
			requestFocus();
			requestFocusInWindow();
			 */
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}
	}

	/**
	 * Reagiert auf Mausbewegungen (Drag&amp;drop und Infos für Tooltips beim
	 * Überfahren von Elementen usw.) auf der Zeichenfläche
	 */
	private class ModelSurfacePanelMouseMotionListener implements MouseMotionListener {
		@Override
		public void mouseDragged(MouseEvent e) {
			if ((readOnly && !allowChangeOperationsOnReadOnly) || dragStartMousePosition==null) return;
			if (dragElement==null) {
				/* Rahmen aufziehen */
				if (readOnly) return;
				int x=Math.min(dragStartMousePosition.x,e.getPoint().x);
				int y=Math.min(dragStartMousePosition.y,e.getPoint().y);
				int w=Math.abs(dragStartMousePosition.x-e.getPoint().x);
				int h=Math.abs(dragStartMousePosition.y-e.getPoint().y);
				selectBox=new Rectangle(x,y,w,h);
				repaint();
			} else {
				if (!dragElement.isUserMoveable() || dragStartElementPosition==null) return;

				GlassInfo.info(getParent(),Language.tr("Editor.SurfaceTooltip.Glass.DragElement"),500);

				if (e.isControlDown() && dragElement.canCopy() && !readOnly) {
					/* Kopie platzieren */
					final Point dragPos=dragElement.getPosition(false);
					if (dragPos.x!=dragStartElementPosition.x || dragPos.y!=dragStartElementPosition.y) {
						/* Aufräumen: Zurückversetzen */
						dragElement.setPosition(dragStartElementPosition);
						final Point subDelta=new Point(dragStartElementPosition.x-dragPos.x,dragStartElementPosition.y-dragPos.y);
						for (ModelElement element : surface.getSelectedArea(true)) if (element instanceof ModelElementPosition && element!=dragElement) {
							final Point point=((ModelElementPosition)element).getPosition(false);
							point.translate(subDelta.x,subDelta.y);
							((ModelElementPosition)element).setPosition(point);
						}
					}
					if (dragCopyElement==null) {
						/* Kopie anlegen */
						dragCopyElement=dragElement.clone(model,surface);
						dragCopyElement.setId(surface.getNextFreeId());
						surface.add(dragCopyElement);
					}
					/* Element platzieren */
					Point delta=new Point(e.getPoint());
					delta.translate(-dragStartMousePosition.x,-dragStartMousePosition.y);
					Point point=new Point(dragStartElementPosition);
					point.translate((int)Math.round(delta.x/zoom),(int)Math.round(delta.y/zoom));
					if (!e.isShiftDown()) {
						point.x=5*(int)Math.round(point.x/5.0);
						point.y=5*(int)Math.round(point.y/5.0);
					}
					dragCopyElement.setPosition(point);
				} else {
					/* Elemente verschieben */
					setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
					if (dragCopyElement!=null) {
						/* Aufräumen: Kopie entfernen */
						surface.remove(dragCopyElement);
						dragCopyElement=null;
					}
					Point delta=new Point(e.getPoint());
					delta.translate(-dragStartMousePosition.x,-dragStartMousePosition.y);
					Point point=new Point(dragStartElementPosition);
					point.translate((int)Math.round(delta.x/zoom),(int)Math.round(delta.y/zoom));
					Point subDelta;
					if (dragStartElementBorderPointNr>=0) {
						subDelta=dragElement.getBorderPointPosition(dragStartElementBorderPointNr);
					} else {
						subDelta=dragElement.getPosition(false);
					}
					if (!e.isShiftDown()) {
						point.x=5*(int)Math.round(point.x/5.0);
						point.y=5*(int)Math.round(point.y/5.0);
					}

					subDelta.x=point.x-subDelta.x;
					subDelta.y=point.y-subDelta.y;
					operationRunning=true;
					try {
						if (dragStartElementBorderPointNr>=0) {
							dragElement.setBorderPointPosition(dragStartElementBorderPointNr,point);
						} else {
							if (point.x<0) point.x=0;
							if (point.y<0) point.y=0;
							dragElement.setPosition(point);
							for (ModelElement element : surface.getSelectedArea(true)) if (element instanceof ModelElementPosition && element!=dragElement) {
								point=((ModelElementPosition)element).getPosition(false);
								point.translate(subDelta.x,subDelta.y);
								if (point.x<0) point.x=0;
								if (point.y<0) point.y=0;
								((ModelElementPosition)element).setPosition(point);
							}
						}
					} finally {
						operationRunning=false;
						fireStateChangeListener();
					}
				}
			}
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			boolean ok;
			ModelElement element;
			String info;
			String tooltip;
			if (selectBox!=null) {
				/* Wenn beim Loslassen der Maustaste irgendwie das Entfernen des Selektionsrahmens unterlassen wurde. */
				selectBox=null;
				repaint();
			}
			switch (mode) {
			case MODE_NORMAL:
				element=surface.getElementAtPosition(e.getPoint(),zoom);
				if (element!=null && !surface.isVisibleOnLayer(element)) element=null;
				if (element==null) {
					setToolTipText(null);
					final boolean handCursor=(emptyScreenLinkPosition(e.getPoint())!=null);
					setCursor(handCursor?Cursor.getPredefinedCursor(Cursor.HAND_CURSOR):Cursor.getDefaultCursor());
				} else {
					tooltip=null;
					if (element instanceof ModelElementPosition) tooltip=((ModelElementPosition)element).getToolTip();
					if (tooltip==null) tooltip=element.getContextMenuElementName();
					String description="";
					if (element instanceof ModelElementBox) {
						final String additional=getAdditionalTooltip((ModelElementBox)element);
						if (additional!=null && !additional.trim().isEmpty()) tooltip=tooltip+"<br>"+additional;
						description=getTooltipDescription((ModelElementBox)element);
						if (description==null) description="";
						if (!description.isEmpty()) description="<hr>"+description;
					}
					if (element instanceof ModelElementEdge) {
						ModelElement el;
						final ModelElementEdge edge=(ModelElementEdge)element;
						final StringBuilder descriptionBuilder=new StringBuilder();
						descriptionBuilder.append("<hr>\n<p>");
						descriptionBuilder.append(Language.tr("Surface.Connection.Tooltip.From"));
						descriptionBuilder.append(": ");
						el=edge.getConnectionStart();
						descriptionBuilder.append(el.getContextMenuElementName());
						if (!el.getName().isEmpty()) {
							descriptionBuilder.append(" (");
							descriptionBuilder.append(el.getName());
							descriptionBuilder.append(")");
						}
						descriptionBuilder.append(", id=");
						descriptionBuilder.append(el.getId());

						descriptionBuilder.append("\n<br>");
						descriptionBuilder.append(Language.tr("Surface.Connection.Tooltip.To"));
						descriptionBuilder.append(": ");
						el=edge.getConnectionEnd();
						descriptionBuilder.append(el.getContextMenuElementName());
						if (!el.getName().isEmpty()) {
							descriptionBuilder.append(" (");
							descriptionBuilder.append(el.getName());
							descriptionBuilder.append(")");
						}
						descriptionBuilder.append(", id=");
						descriptionBuilder.append(el.getId());
						descriptionBuilder.append("<br>&nbsp;\n</p>");
						description=descriptionBuilder.toString();
					}
					setToolTipText("<html>"+tooltip+"<br><b>id="+element.getId()+"</b>"+description+"</html>");
					final int borderPoint=element.getBorderPointNr(new Point((int)Math.round(e.getPoint().x/zoom),(int)Math.round(e.getPoint().y/zoom)));
					switch (borderPoint) {
					case 0: setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)); break;
					case 1: setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)); break;
					case 2: setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR)); break;
					case 3: setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR)); break;
					default: setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); break;
					}
				}
				break;
			case MODE_ADD_ELEMENT:
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				setToolTipText(null);
				break;
			case MODE_ADD_EDGE_STEP1:
				ok=false;
				element=surface.getElementAtPosition(e.getPoint(),zoom);
				if (element instanceof ModelElementPosition) ok=((ModelElementPosition)element).canAddEdgeOut();
				setCursor(ok?Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR):cursorNotAllowed);
				tooltip=null;
				if (element instanceof ModelElementPosition)  tooltip=((ModelElementPosition)element).getToolTip();
				if (tooltip==null) tooltip=(element==null)?"":element.getContextMenuElementName();
				info=(element==null)?"":(tooltip+" (id="+element.getId()+")<br>");
				if (ok) setToolTipText("<html>"+info+"<span style=\"color: green;\"><b>"+Language.tr("Editor.SurfaceTooltip.InsertEdge1")+"</b></span></html>"); else setToolTipText("<html>"+info+"<b><span style=\"color: red\">"+Language.tr("Editor.SurfaceTooltip.NotEdge1")+"</b></span></html>");

				break;
			case MODE_ADD_EDGE_STEP2:
				ok=false;
				element=surface.getElementAtPosition(e.getPoint(),zoom);
				if (element instanceof ModelElementPosition) ok=((ModelElementPosition)element).canAddEdgeIn();
				setCursor(ok?Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR):cursorNotAllowed);
				tooltip=null;
				if (element instanceof ModelElementPosition)  tooltip=((ModelElementPosition)element).getToolTip();
				if (tooltip==null) tooltip=(element==null)?"":element.getContextMenuElementName();
				info=(element==null)?"":(tooltip+" (id="+element.getId()+")<br>");
				if (ok) setToolTipText("<html>"+info+"<span style=\"color: green;\"><b>"+Language.tr("Editor.SurfaceTooltip.InsertEdge2")+"</b></span></html>"); else setToolTipText("<html>"+info+"<b><span style=\"color: red\">"+Language.tr("Editor.SurfaceTooltip.NotEdge2")+"</b></span></html>");
				break;
			case MODE_INSERT_ELEMENTS_FROM_CLIPBOARD:
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				setToolTipText(Language.tr("Editor.SurfaceTooltip.PasteFromClipboard"));
				break;
			case MODE_INSERT_ELEMENTS_FROM_TEMPLATE:
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				setToolTipText(Language.tr("Editor.SurfaceTooltip.PasteFromTemplate"));
				break;
			case MODE_INSERT_IMAGE:
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				setToolTipText(Language.tr("Editor.SurfaceTooltip.PasteFromClipboard.Image"));
				break;
			case MODE_INSERT_TEXT:
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				setToolTipText(Language.tr("Editor.SurfaceTooltip.PasteFromClipboard.Text"));
				break;
			}
		}
	}

	/**
	 * Reagiert auf Mausrad-Bewegungen auf der Zeichenfläche
	 */
	private class ModelSurfacePanelMouseWheelListener implements MouseWheelListener {
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			if (e.isControlDown()) {
				if (Math.abs(e.getWheelRotation())==1) {
					/* Rad */
					setZoom(Math.max(ZOOM_MIN,Math.min(ZOOM_MAX,zoom-ZOOM_STEP*e.getWheelRotation())));
				}
			} else {
				if (getParent() instanceof JViewport) {
					JViewport viewport=(JViewport)getParent();
					Point point=viewport.getViewPosition();
					if (e.isAltDown()) {
						if (Math.abs(e.getWheelRotation())==1) {
							/* Rad */
							point.x+=viewport.getWidth()/WHEEL_SCROLL_FRACTION*e.getWheelRotation();
							if (point.x<0) point.x=0;
						} else {
							/* Rad-Klick */
							point.y+=viewport.getWidth()/WHEEL_SCROLL_FRACTION*Math.signum(e.getWheelRotation());
							if (point.y<0) point.y=0;
						}
					} else {
						if (Math.abs(e.getWheelRotation())==1) {
							/* Rad */
							point.y+=viewport.getHeight()/WHEEL_SCROLL_FRACTION*e.getWheelRotation();
							if (point.y<0) point.y=0;
						} else {
							/* Rad-Klick */
							point.x+=viewport.getWidth()/WHEEL_SCROLL_FRACTION*Math.signum(e.getWheelRotation());
							if (point.x<0) point.x=0;
						}
					}
					viewport.setViewPosition(point);
				}
			}
			e.consume();
			repaint();

			GlassInfo.info(getParent(),Language.tr("Editor.SurfaceTooltip.Glass.MouseWheel"),500);
		}
	}

	/**
	 * Reagiert auf Tastendrücken auf der Zeichenfläche
	 */
	private class ModelSurfacePanelKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {
		}

		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode()==KeyEvent.VK_CONTEXT_MENU) {
				final ModelElement element=surface.getSelectedElement();
				if (element!=null) {
					surface.setSelectedArea(null,zoom);
					fireStateChangeListener();
					fireSelectionListener();
					Point point=element.getPosition(false);
					point.x=(int)Math.round(point.x*zoom);
					point.y=(int)Math.round(point.y*zoom);
					point.translate(10,10);
					element.setModel(model);
					element.showContextMenu(ModelSurfacePanel.this,point,readOnly,allowChangeOperationsOnReadOnly,(e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK)==InputEvent.SHIFT_DOWN_MASK,ModelSurfacePanel.this,template->fireBuildParameterSeries(template),clientData,sequences,simData,mainAnimator);
				} else {
					if (showEditModelProperties) {
						Container c=getParent();
						if (c!=null) {
							c=c.getParent();
							if (c!=null) showModelContextMenu(new Point(c.getWidth()/2,c.getHeight()/2));
						}
					}
				}
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_DELETE && !e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
				if (!readOnly) deleteSelectedElements();
				fireStateChangeListener();
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_ENTER && e.isControlDown() && !e.isShiftDown() && !e.isAltDown()) {
				showElementProperties(surface.getSelectedElement(),0);
				fireStateChangeListener();
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_ENTER && e.isControlDown() && e.isShiftDown() && !e.isAltDown()) {
				final ModelElement element=surface.getSelectedElement();
				if (element!=null && element.hasAnimationStatisticsData(simData)) element.showElementAnimationStatisticsData(ModelSurfacePanel.this,simData);
				fireStateChangeListener();
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_ENTER && !e.isControlDown() && e.isShiftDown() && !e.isAltDown()) {
				showElementSubEditor(surface.getSelectedElement());
				fireStateChangeListener();
				e.consume();
				return;
			}

			if ((e.getKeyCode()==KeyEvent.VK_LEFT || e.getKeyCode()==KeyEvent.VK_RIGHT || e.getKeyCode()==KeyEvent.VK_UP || e.getKeyCode()==KeyEvent.VK_DOWN) && e.isAltDown() && !e.isControlDown()) {
				ModelElement element=surface.getSelectedElement();
				if (element==null) {
					List<ModelElement> list=surface.getSelectedArea(true);
					if (list!=null && list.size()>0) element=list.get(0);
				}

				if (element instanceof ModelElementPosition && !(readOnly && !allowChangeOperationsOnReadOnly)) {
					Point delta;
					switch (e.getKeyCode()) {
					case KeyEvent.VK_LEFT: delta=new Point(-1,0); break;
					case KeyEvent.VK_RIGHT: delta=new Point(1,0); break;
					case KeyEvent.VK_UP: delta=new Point(0,-1); break;
					case KeyEvent.VK_DOWN: delta=new Point(0,1); break;
					default: delta=new Point(1,0); break;
					}
					if (!e.isShiftDown()) {delta.x*=5; delta.y*=5;}
					final Point point=element.getPosition(false);
					final Point pointOrig=new Point(point);
					point.x=Math.max(point.x+delta.x,0);
					point.y=Math.max(point.y+delta.y,0);
					if (!e.isShiftDown()) {
						point.x=5*(int)Math.round(point.x/5.0);
						point.y=5*(int)Math.round(point.y/5.0);
					}
					element.setPosition(point);
					final int xShift=point.x-pointOrig.x;
					final int yShift=point.y-pointOrig.y;
					for (ModelElement areaElement: surface.getSelectedArea(true)) if (areaElement!=element && areaElement instanceof ModelElementPosition) {
						final Point p=areaElement.getPosition(false);
						p.translate(xShift,yShift);
						areaElement.setPosition(p);
					}
					repaint();
					fireStateChangeListener();
					e.consume();
					return;
				}
			}

			if ((e.getKeyCode()==KeyEvent.VK_C || e.getKeyCode()==KeyEvent.VK_INSERT) && e.isControlDown() && !e.isAltDown() && !e.isShiftDown()) {
				copyToClipboard();
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_V && e.isControlDown() && !e.isAltDown() && !e.isShiftDown() && !readOnly) {
				pasteFromClipboard();
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_INSERT && !e.isControlDown() && !e.isAltDown() && e.isShiftDown() && !readOnly) {
				pasteFromClipboard();
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_PAGE_UP) {
				moveSelectedElementToFront(e.isControlDown());
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_PAGE_DOWN) {
				moveSelectedElementToBack(e.isControlDown());
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_ESCAPE && mode==ClickMode.MODE_ADD_EDGE_STEP2) {
				setMode(ClickMode.MODE_ADD_EDGE_STEP1);
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_ESCAPE && mode==ClickMode.MODE_ADD_EDGE_STEP1) {
				setMode(ClickMode.MODE_NORMAL);
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_ESCAPE && mode==ClickMode.MODE_NORMAL && !readOnly) {
				setElementTemplatesVisibility(ElementTemplatesVisibility.TOGGLE);
				e.consume();
				return;
			}

			if (e.getKeyCode()==KeyEvent.VK_1 && e.isControlDown() && !readOnly) {
				ModelElement element=surface.getSelectedElement();
				if (element==null) {
					List<ModelElement> list=surface.getSelectedArea(true);
					if (list!=null && list.size()>0) element=list.get(0);
				}
				if (element instanceof ModelElementPosition) {
					showQuickFixPopup((ModelElementPosition)element);
					e.consume();
				}
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
		}
	}
}