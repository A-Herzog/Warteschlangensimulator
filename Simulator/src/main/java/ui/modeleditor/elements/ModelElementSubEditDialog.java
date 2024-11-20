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
package ui.modeleditor.elements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import language.Language;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import tools.ImagePrintable;
import ui.EditorPanel;
import ui.help.Help;
import ui.images.Images;
import ui.infopanel.InfoPanel;
import ui.modeleditor.ModelClientData;
import ui.modeleditor.ModelSurface;
import ui.modeleditor.coreelements.ModelElement;
import ui.tools.GlassInfo;
import ui.tools.WindowSizeStorage;

/**
 * In diesem Dialog kann ein in einem {@link ModelElementSub}-Element
 * enthaltenes Unter-Modell bearbeitet werden.
 * @author Alexander Herzog
 * @see ModelElementSub
 */
public class ModelElementSubEditDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = -6057942278443843417L;

	/** Editor-Panel in dem das Untermodell bearbeitet werden kann */
	private final EditorPanel editorPanel;

	/**
	 * Konstruktor der Klasse <code>ModelElementSubEditDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param id	ID des Sub-Elements, dessen Inhalt hier bearbeitet werden soll
	 * @param model	Element vom Typ <code>EditModel</code> (wird ben�tigt, um die Liste der globalen Variablen zu laden)
	 * @param mainSurface	Surface der obersten Ebene (enth�lt Ressourcen usw.)
	 * @param subSurface	Zu bearbeitendes Surface
	 * @param edgesIn	In das untergeordnete Modell einlaufende Ecken (mit ids)
	 * @param edgesOut	Aus dem untergeordneten Modell auslaufende Ecken (mit ids)
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param wasTriggeredViaEditDialog	Wurde der Dialog auf dem Umweg �ber den Untermodell-Bearbeiten-Dialog aufgerufen? (Wenn ja, wird auf der Untermodell-Zeichenfl�che ein Hinweis zum direkten Aufruf angezeigt.)
	 * @param isFullSubModel	Handelt es sich um ein vollwertiges Untermodell (<code>true</code>) oder um die Diagramm-Anzeige (<code>false</code>)
	 */
	public ModelElementSubEditDialog(final Component owner, final int id, final EditModel model, final ModelSurface mainSurface, final ModelSurface subSurface, final int[] edgesIn, final int[] edgesOut, final boolean readOnly, final boolean wasTriggeredViaEditDialog, final boolean isFullSubModel) {
		super(owner,isFullSubModel?Language.tr("Surface.Sub.Dialog.Title"):Language.tr("Surface.Dashboard.Dialog.Title"),readOnly);

		/* Modell */
		final EditModel ownModel=model.clone();
		ownModel.resources=mainSurface.getResources().clone();
		ownModel.surface=subSurface.clone(false,null,null,mainSurface,model);

		/* Beschriftungen der Ein- und Ausg�nge anpassen */
		int countIn=0;
		int countOut=0;
		for (ModelElement element: ownModel.surface.getElements()) {
			if (element instanceof ModelElementSubIn) {
				if (countIn<edgesIn.length) {
					final ModelElementSubIn in=(ModelElementSubIn)element;
					final ModelElement previous=mainSurface.getById(edgesIn[countIn]);
					if (previous!=null) in.setConnectionData(countIn,previous.getId());
				}
				countIn++;

			}
			if (element instanceof ModelElementSubOut) {
				if (countOut<edgesOut.length) {
					final ModelElementSubOut out=(ModelElementSubOut)element;
					final ModelElement next=mainSurface.getById(edgesOut[countOut]);
					if (next!=null) out.setConnectionData(countOut,next.getId());
				}
				countOut++;
			}
		}

		/* Export-Button */
		addUserButton(Language.tr("Surface.Sub.Dialog.Export"),(Icon)null);

		/* GUI */
		final JPanel all=createGUI(()->Help.topicModal(ModelElementSubEditDialog.this,isFullSubModel?"ModelElementSub":"ModelElementDashboard"));
		all.setLayout(new BorderLayout());
		InfoPanel.addTopPanel(all,isFullSubModel?InfoPanel.stationSub:InfoPanel.stationDashboard);
		final JPanel content=new JPanel(new BorderLayout());
		all.add(content,BorderLayout.CENTER);

		final InputMap im=content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		final ActionMap am=content.getActionMap();

		/* Toolbar oben */
		if (!readOnly) {
			final JToolBar toolbar=new JToolBar(SwingConstants.HORIZONTAL);
			content.add(toolbar,BorderLayout.NORTH);
			final JButton editButton=new JButton(Language.tr("Main.Menu.Edit"),Images.GENERAL_EDIT.getIcon());
			toolbar.add(editButton);
			editButton.addActionListener(e->showEditMenu(editButton));
		}

		/* Zeichenfl�che */
		content.add(editorPanel=new EditorPanel(this,ownModel,false,readOnly,false,false),BorderLayout.CENTER);
		editorPanel.setSavedViewsButtonVisible(false);
		if (!isFullSubModel) editorPanel.setRestrictedCatalog(true);

		/* Hotkeys */
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A,InputEvent.CTRL_DOWN_MASK),"EditSelectAll");
		am.put("EditSelectAll",new EditorPanel.FunctionalAction(()->processMenuCommand("EditSelectAll")));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK),"EditCopyModel");
		am.put("EditCopyModel",new EditorPanel.FunctionalAction(()->processMenuCommand("EditCopyModel")));

		if (!readOnly) {
			im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X,InputEvent.CTRL_DOWN_MASK),"EditCut");
			am.put("EditCut",new EditorPanel.FunctionalAction(()->processMenuCommand("EditCut")));
		}

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SUBTRACT,InputEvent.CTRL_DOWN_MASK),"zoomOut");
		am.put("zoomOut",new EditorPanel.FunctionalAction(()->editorPanel.zoomOut()));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ADD,InputEvent.CTRL_DOWN_MASK),"zoomIn");
		am.put("zoomIn",new EditorPanel.FunctionalAction(()->editorPanel.zoomIn()));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MULTIPLY,InputEvent.CTRL_DOWN_MASK),"zoomDefault");
		am.put("zoomDefault",new EditorPanel.FunctionalAction(()->editorPanel.zoomDefault()));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0,InputEvent.CTRL_DOWN_MASK),"center");
		am.put("center",new EditorPanel.FunctionalAction(()->editorPanel.centerModel()));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,InputEvent.CTRL_DOWN_MASK),"scrollTop");
		am.put("scrollTop",new EditorPanel.FunctionalAction(()->editorPanel.scrollToTop()));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0),"templates");
		am.put("templates",new EditorPanel.FunctionalAction(()->editorPanel.setTemplatesVisible(!editorPanel.isTemplatesVisible(),false)));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12,0),"navigator");
		am.put("navigator",new EditorPanel.FunctionalAction(()->editorPanel.setNavigatorVisible(!editorPanel.isNavigatorVisible(),false)));

		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12,InputEvent.CTRL_DOWN_MASK),"explorer");
		am.put("explorer",new EditorPanel.FunctionalAction(()->editorPanel.showExplorer()));

		/* Dialog starten */
		setSizeRespectingScreensize(1024,768);
		setResizable(true);
		setMinSizeRespectingScreensize(800,600);
		setLocationRelativeTo(getOwner());
		WindowSizeStorage.window(this,""+id);

		if (wasTriggeredViaEditDialog) {
			GlassInfo.info(this,Language.tr("Surface.Sub.Dialog.DirectAccessHint"),500);
		}
	}

	/**
	 * Bereitet die Untermodell-Zeichenfl�che vor.
	 * @param model	Element vom Typ <code>EditModel</code> (wird ben�tigt, um die Liste der globalen Variablen zu laden)
	 * @param mainSurface	Surface der obersten Ebene (enth�lt Ressourcen usw.)
	 * @param original	Original Untermodell-Zeichenfl�che
	 * @param readOnly	Wird dieser Parameter auf <code>true</code> gesetzt, so wird die "Ok"-Schaltfl�che deaktiviert
	 * @param edgesIn	IDs der von au�en einlaufenden Kanten
	 * @param edgesOut	IDs der nach au�en auslaufenden Kanten
	 * @return	Neue Untermodell-Zeichenfl�che
	 */
	public static ModelSurface prepareSurface(final EditModel model, final ModelSurface mainSurface, final ModelSurface original, final boolean readOnly, final int[] edgesIn, final int[] edgesOut) {
		ModelSurface prepared=original.clone(false,null,null,mainSurface,model);

		if (!readOnly) {

			/* �berz�hlige Ein- und Ausg�nge l�schen */
			List<ModelElement> deleteElements=new ArrayList<>();
			int countIn=0;
			int countOut=0;
			for (ModelElement element: prepared.getElements()) {
				if (element instanceof ModelElementSubIn) {
					countIn++;
					if (countIn>edgesIn.length) deleteElements.add(element); else {
						((ModelElementSubConnect)element).setConnectionData(countIn-1,edgesIn[countIn-1]);
					}
				}
				if (element instanceof ModelElementSubOut) {
					countOut++;
					if (countOut>edgesOut.length) deleteElements.add(element); else {
						((ModelElementSubConnect)element).setConnectionData(countOut-1,edgesOut[countOut-1]);
					}
				}
			}

			for (ModelElement element: deleteElements) prepared.remove(element);

			Point pos=new Point(50,50);
			/* Fehlende Verbindungen anlegen */
			for (int i=countIn;i<edgesIn.length;i++) {
				final ModelElementSubIn connectionIn=new ModelElementSubIn(model,prepared,i,edgesIn[i]);
				while (prepared.getElementAtPosition(pos,1.0)!=null) pos.x+=50;
				connectionIn.setPosition(pos);
				pos.x+=150;
				prepared.add(connectionIn);
			}
			for (int i=countOut;i<edgesOut.length;i++) {
				final ModelElementSubOut connectionOut=new ModelElementSubOut(model,prepared,i,edgesOut[i]);
				while (prepared.getElementAtPosition(pos,1.0)!=null) pos.x+=50;
				connectionOut.setPosition(pos);
				pos.x+=150;
				prepared.add(connectionOut);
			}
		}

		/* Ebenen */
		prepared.getLayers().clear();
		prepared.getLayers().addAll(mainSurface.getLayers());
		prepared.getVisibleLayers().clear();
		prepared.getVisibleLayers().addAll(mainSurface.getVisibleLayers());
		prepared.setActiveLayer(mainSurface.getActiveLayer());

		return prepared;
	}

	/**
	 * Liefert das Untermodell nach dem Schlie�en des Dialogs zur�ck
	 * @return	Untergeordnetes Modell
	 */
	public ModelSurface getSurface() {
		final EditModel model=editorPanel.getModel();
		model.surface.setSelectedElement(null);
		return model.surface;
	}

	/**
	 * Liefert die m�glicherweise ver�nderten Kunden-Einstellungen.
	 * @return	Kunden-Einstellungen
	 */
	public ModelClientData getClientData() {
		return editorPanel.getModel().clientData;
	}

	/**
	 * Pr�ft, ob die Zeichenfl�che �berhaupt Element, die exportiert werden k�nnten, enth�lt
	 * und gibt wenn nicht eine Fehlermeldung aus.
	 * @param message	Text f�r die Fehlermeldung
	 * @return	Liefert <code>true</code>, wenn die Zeichenfl�che Element enth�lt
	 */
	private boolean canExport(final String message) {
		if (editorPanel.getOriginalSurface().getElementCount()==0) {
			MsgBox.error(this,Language.tr("Surface.Sub.Dialog.Export.SurfaceEmpty"),message);
			return false;
		}
		return true;
	}

	@Override
	protected void userButtonClick(final int nr, final JButton button) {
		final JPopupMenu popup=new JPopupMenu();

		JMenuItem item;

		popup.add(item=new JMenuItem(Language.tr("Surface.Sub.Dialog.Export.Copy"),Images.EDIT_COPY_AS_IMAGE.getIcon()));
		item.addActionListener(e->{
			if (!canExport(Language.tr("Surface.Sub.Dialog.Export.CopyError"))) return;
			editorPanel.exportModelToClipboard();
		});

		popup.add(item=new JMenuItem(Language.tr("Surface.Sub.Dialog.Export.Save"),Images.GENERAL_SAVE.getIcon()));
		item.addActionListener(e->{
			if (!canExport(Language.tr("Surface.Sub.Dialog.Export.SaveError"))) return;
			String error=editorPanel.exportModelToFile(null,false);
			if (error!=null) MsgBox.error(this,Language.tr("XML.ExportErrorTitle"),error);
		});

		popup.add(item=new JMenuItem(Language.tr("Surface.Sub.Dialog.Export.Print"),Images.GENERAL_PRINT.getIcon()));
		item.addActionListener(e->{
			if (!canExport(Language.tr("Surface.Sub.Dialog.Export.PrintError"))) return;
			ImagePrintable.print(editorPanel.getPrintImage(2000));
		});

		popup.show(button,0,button.getHeight());
	}

	/**
	 * Stellt ein hervorzuhebendes Zeichen f�r den Men�punkt ein
	 * @param menu	Men�punkt
	 * @param languageString	Der erste Buchstabe dieses Strings wird in dem Men�punkt hervorgehoben
	 */
	private static void setMnemonic(final JMenuItem menu, final String languageString) {
		if (languageString==null || languageString.isBlank()) return;
		final char c=languageString.charAt(0);
		if (c!=' ') menu.setMnemonic(c);
	}

	/**
	 * Erstellt ein Men�punkt-Objekt.
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll (kann <code>null</code> sein, f�r kein Hotkey)
	 * @param hotkeyMask Strg oder Umschalt (oder beides) f�r den Men�punkt-Hotkey
	 * @param actionCommand	Auszuf�hrender Befehl
	 * @return	Neuer Men�punkt
	 */
	private JMenuItem buildMenuItem(final String title, final Icon icon, final String mnemonic, final int hotkey, final int hotkeyMask, final String actionCommand) {
		final JMenuItem item=new JMenuItem(title);
		if (icon!=null) item.setIcon(icon);
		setMnemonic(item,mnemonic);
		if (hotkey!=0) item.setAccelerator(KeyStroke.getKeyStroke(hotkey,hotkeyMask));
		item.addActionListener(e->processMenuCommand(actionCommand));
		return item;
	}

	/**
	 * Erstellt ein Men�punkt-Objekt und f�gt es in ein Men� ein.
	 * @param parent	�bergeordnetes Men�
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll (kann <code>null</code> sein, f�r kein Hotkey)
	 * @param actionCommand	Auszuf�hrender Befehl
	 * @return	Neuer Men�punkt
	 */
	private JMenuItem createMenuItem(final JPopupMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=buildMenuItem(title,icon,mnemonic,hotkey,0,actionCommand);
		parent.add(item);
		return item;
	}

	/**
	 * Erstellt ein Men�punkt-Objekt und f�gt es in ein Men� ein.
	 * @param parent	�bergeordnetes Men�
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll (kann <code>null</code> sein, f�r kein Hotkey)
	 * @param actionCommand	Auszuf�hrender Befehl
	 * @return	Neuer Men�punkt
	 */
	private JMenuItem createMenuItem(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=buildMenuItem(title,icon,mnemonic,hotkey,0,actionCommand);
		parent.add(item);
		return item;
	}

	/**
	 * Erstellt ein Men�punkt-Objekt und f�gt es in ein Men� ein.
	 * @param parent	�bergeordnetes Men�
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll (kann <code>null</code> sein, f�r kein Hotkey); Hotkey wird mit "Strg" kombiniert
	 * @param actionCommand	Auszuf�hrender Befehl
	 * @return	Neuer Men�punkt
	 */
	private JMenuItem createMenuItemCtrl(final JPopupMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=buildMenuItem(title,icon,mnemonic,hotkey,InputEvent.CTRL_DOWN_MASK,actionCommand);
		parent.add(item);
		return item;
	}

	/**
	 * Erstellt ein Men�punkt-Objekt und f�gt es in ein Men� ein.
	 * @param parent	�bergeordnetes Men�
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll (kann <code>null</code> sein, f�r kein Hotkey); Hotkey wird mit "Strg" kombiniert
	 * @param actionCommand	Auszuf�hrender Befehl
	 * @return	Neuer Men�punkt
	 */
	private JMenuItem createMenuItemCtrl(final JMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=buildMenuItem(title,icon,mnemonic,hotkey,InputEvent.CTRL_DOWN_MASK,actionCommand);
		parent.add(item);
		return item;
	}

	/**
	 * Erstellt ein Men�punkt-Objekt und f�gt es in ein Men� ein.
	 * @param parent	�bergeordnetes Men�
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll (kann <code>null</code> sein, f�r kein Hotkey); Hotkey wird mit "Umschalt" kombiniert
	 * @param actionCommand	Auszuf�hrender Befehl
	 * @return	Neuer Men�punkt
	 */
	private JMenuItem createMenuItemShift(final JPopupMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=buildMenuItem(title,icon,mnemonic,hotkey,InputEvent.SHIFT_DOWN_MASK,actionCommand);
		parent.add(item);
		return item;
	}


	/**
	 * Erstellt ein Men�punkt-Objekt und f�gt es in ein Men� ein.
	 * @param parent	�bergeordnetes Men�
	 * @param title	Name des neuen Men�punkts
	 * @param icon	Pfad zu dem Icon, das neben dem neuen Men�punkt angezeigt werden soll (kann <code>null</code> sein, wenn kein Icon angezeigt werden soll)
	 * @param mnemonic	Hervorzuhebender Buchstabe in dem Namen des Men�punkts (kann <code>'\0'</code> sein, wenn nichts hervorgehoben werden soll)
	 * @param hotkey	Taste, die als Hotkey f�r den Men�punkt verwendet werden soll (kann <code>null</code> sein, f�r kein Hotkey); Hotkey wird mit "Strg"+"Umschalt" kombiniert
	 * @param actionCommand	Auszuf�hrender Befehl
	 * @return	Neuer Men�punkt
	 */
	private JMenuItem createMenuItemCtrlShift(final JPopupMenu parent, final String title, final Icon icon, final String mnemonic, final int hotkey, final String actionCommand) {
		final JMenuItem item=buildMenuItem(title,icon,mnemonic,hotkey,InputEvent.CTRL_DOWN_MASK+InputEvent.SHIFT_DOWN_MASK,actionCommand);
		parent.add(item);
		return item;
	}

	/**
	 * Zeigt das Bearbeiten-Men� an.
	 * @param invoker	Aufrufer (Schaltfl�che an der das Men� ausgerichtet werden soll)
	 */
	private void showEditMenu(final JComponent invoker) {
		final JPopupMenu menu=new JPopupMenu();

		JMenu submenu;

		createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.Cut"),Images.EDIT_CUT.getIcon(),Language.tr("Main.Menu.Edit.Cut.Mnemonic"),KeyEvent.VK_X,"EditCut");
		createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.Copy"),Images.EDIT_COPY.getIcon(),Language.tr("Main.Menu.Edit.Copy.Mnemonic"),KeyEvent.VK_C,"EditCopy");
		createMenuItemCtrlShift(menu,Language.tr("Main.Menu.Edit.CopyAsImage"),Images.EDIT_COPY_AS_IMAGE.getIcon(),Language.tr("Main.Menu.Edit.CopyAsImage.Mnemonic"),KeyEvent.VK_C,"EditCopyModel");
		createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.Paste"),Images.EDIT_PASTE.getIcon(),Language.tr("Main.Menu.Edit.Paste.Mnemonic"),KeyEvent.VK_V,"EditPaste");
		createMenuItem(menu,Language.tr("Main.Menu.Edit.Delete"),Images.EDIT_DELETE.getIcon(),Language.tr("Main.Menu.Edit.Delete.Mnemonic"),KeyEvent.VK_DELETE,"EditDelete");
		createMenuItemShift(menu,Language.tr("Main.Menu.Edit.DeleteAndCloseGap"),Images.EDIT_EDGES_ADD.getIcon(),Language.tr("Main.Menu.Edit.DeleteAndCloseGap.Mnemonic"),KeyEvent.VK_DELETE,"EditDeleteAndCloseGap");
		createMenuItemCtrl(menu,Language.tr("Main.Menu.Edit.SelectAll"),null,Language.tr("Main.Menu.Edit.SelectAll.Mnemonic"),KeyEvent.VK_A,"EditSelectAll");
		menu.addSeparator();
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Edit.Arrange")));
		createMenuItemCtrl(submenu,Language.tr("Main.Menu.Edit.MoveFront"),Images.MOVE_FRONT.getIcon(),Language.tr("Main.Menu.Edit.MoveFront.Mnemonic"),KeyEvent.VK_PAGE_UP,"EditSendFront");
		createMenuItem(submenu,Language.tr("Main.Menu.Edit.MoveForwards"),Images.MOVE_FRONT_STEP.getIcon(),Language.tr("Main.Menu.Edit.MoveForwards.Mnemonic"),KeyEvent.VK_PAGE_UP,"EditSendForwards");
		createMenuItem(submenu,Language.tr("Main.Menu.Edit.MoveBackwards"),Images.MOVE_BACK_STEP.getIcon(),Language.tr("Main.Menu.Edit.MoveBackwards.Mnemonic"),KeyEvent.VK_PAGE_DOWN,"EditSendBackwards");
		createMenuItemCtrl(submenu,Language.tr("Main.Menu.Edit.MoveBack"),Images.MOVE_BACK.getIcon(),Language.tr("Main.Menu.Edit.MoveBack.Mnemonic"),KeyEvent.VK_PAGE_DOWN,"EditSendBackground");
		menu.add(submenu=new JMenu(Language.tr("Main.Menu.Edit.Align")));
		createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Top"),Images.ALIGN_TOP.getIcon(),Language.tr("Main.Menu.Edit.Align.Top.Mnemonic"),0,"EditAlignTop");
		createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Middle"),Images.ALIGN_MIDDLE.getIcon(),Language.tr("Main.Menu.Edit.Align.Middle.Mnemonic"),0,"EditAlignMiddle");
		createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Bottom"),Images.ALIGN_BOTTOM.getIcon(),Language.tr("Main.Menu.Edit.Align.Bottom.Mnemonic"),0,"AlignBottom");
		submenu.addSeparator();
		createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Left"),Images.ALIGN_LEFT.getIcon(),Language.tr("Main.Menu.Edit.Align.Left.Mnemonic"),0,"EditAlignLeft");
		createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Center"),Images.ALIGN_CENTER.getIcon(),Language.tr("Main.Menu.Edit.Align.Center.Mnemonic"),0,"EditAlignCenter");
		createMenuItem(submenu,Language.tr("Main.Menu.Edit.Align.Right"),Images.ALIGN_RIGHT.getIcon(),Language.tr("Main.Menu.Edit.Align.Right.Mnemonic"),0,"EditAlignRight");

		menu.show(invoker,0,invoker.getHeight());
	}

	/**
	 * F�hrt einen Men�befehl aus.
	 * @param command	Men�befehl
	 * @see #showEditMenu(JComponent)
	 */
	private void processMenuCommand(final String command) {
		if (command.equals("EditCut")) editorPanel.cutSelectedElementsToClipboard();
		if (command.equals("EditCopy")) editorPanel.copySelectedElementsToClipboard();
		if (command.equals("EditCopyModel")) {
			if (editorPanel.getOriginalSurface().getElementCount()==0) {
				MsgBox.error(this,Language.tr("Main.Menu.Edit.CopyAsImage.ErrorTitle"),Language.tr("Main.Menu.Edit.CopyAsImage.ErrorInfo"));
				return;
			}
			editorPanel.exportModelToClipboard();
		}
		if (command.equals("EditPaste")) editorPanel.pasteFromClipboard();
		if (command.equals("EditDelete")) editorPanel.deleteSelectedElements();
		if (command.equals("EditDeleteAndCloseGap")) editorPanel.deleteSelectedElementAndCloseGap();
		if (command.equals("EditSelectAll")) editorPanel.selectAll();
		if (command.equals("EditSendFront")) editorPanel.moveSelectedElementToFront(true);
		if (command.equals("EditSendForwards")) editorPanel.moveSelectedElementToFront(false);
		if (command.equals("EditSendBackwards")) editorPanel.moveSelectedElementToBack(false);
		if (command.equals("EditSendBackground")) editorPanel.moveSelectedElementToBack(true);
		if (command.equals("EditAlignTop")) editorPanel.alignSelectedElementsTop();
		if (command.equals("EditAlignMiddle")) editorPanel.alignSelectedElementsMiddle();
		if (command.equals("EditAlignBottom")) editorPanel.alignSelectedElementsBottom();
		if (command.equals("EditAlignLeft")) editorPanel.alignSelectedElementsLeft();
		if (command.equals("EditAlignCenter")) editorPanel.alignSelectedElementsCenter();
		if (command.equals("EditAlignRight")) editorPanel.alignSelectedElementsRight();
	}
}