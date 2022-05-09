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
package ui.tools;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.distribution.swing.CommonVariables;
import swingtools.ImageIOFormatCheck;
import systemtools.ImageTools;
import systemtools.MsgBox;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;
import ui.modeleditor.ScaledImageCache;

/**
 * Ermöglicht die Auswahl eines Bildes
 * @author Alexander Herzog
 */
public class ImageChooser extends JPanel {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 9151154041290240327L;

	/** Anzuzeigendes Bild */
	private BufferedImage image;
	/** Hash-Wert für das aktuelle Bild (darf <code>null</code> sein, ist nur ein Hint für {@link ScaledImageCache}) */
	private String imageHash;
	/** Benutzerdefinierte Animationsicons */
	private final ModelAnimationImages modelImages;
	/** Gibt an, ob das Bild ausgetauscht (neu laden / einfügen) werden darf */
	private boolean enabled;

	/** Toolbar in dem Panel */
	private JToolBar toolBar;
	/** "Kopieren"-Schaltfläche */
	private JButton buttonCopy;
	/** "Einfügen"-Schaltfläche */
	private JButton buttonPaste;
	/** "Laden"-Schaltfläche */
	private JButton buttonLoad;
	/** "Speichern"-Schaltfläche */
	private JButton buttonSave;
	/** "Vorlagebild auswählen"-Schaltfläche */
	private JButton buttonTemplate;

	/**
	 * Konstruktor der Klasse <code>ImageChooser</code>
	 * @param image	Initial anzuzeigendes Bild (kann <code>null</code> sein)
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 */
	public ImageChooser(final BufferedImage image, final ModelAnimationImages modelImages) {
		super();
		this.image=image;
		imageHash=null;
		this.modelImages=modelImages;
		enabled=true;

		setLayout(new BorderLayout());
		add(new ImageHolderPanel(),BorderLayout.CENTER);
		add(toolBar=new JToolBar(),BorderLayout.SOUTH);
		toolBar.setFloatable(false);
		buttonCopy=addButton(Language.tr("Dialog.Button.Copy"),Language.tr("Dialog.Button.Copy.InfoImage"),Images.EDIT_COPY.getIcon());
		buttonPaste=addButton(Language.tr("Dialog.Button.Paste"),Language.tr("Dialog.Button.Paste.InfoImage"),Images.EDIT_PASTE.getIcon());
		buttonLoad=addButton(Language.tr("Dialog.Button.Load"),Language.tr("Dialog.Button.Load.InfoImage"),Images.IMAGE_LOAD.getIcon());
		buttonSave=addButton(Language.tr("Dialog.Button.Save"),Language.tr("Dialog.Button.Save.InfoImage"),Images.IMAGE_SAVE.getIcon());
		toolBar.addSeparator();
		buttonTemplate=addButton(Language.tr("Dialog.Button.Template"),Language.tr("Dialog.Button.Template.InfoImage"),Images.IMAGE_TEMPLATE.getIcon());
		updateGUI();
		initDropTarget();
	}

	/**
	 * Richtet die Komponente als Drag&amp;drop-Empfänger ein.
	 * @return	Liefert <code>true</code>, wenn die Drag&amp;drop-Einstellungen vorgenommen werden konnten
	 */
	private boolean initDropTarget() {
		final DropTarget dt=new DropTarget();
		setDropTarget(dt);
		try {
			dt.addDropTargetListener(new DropTargetAdapter() {
				@Override
				public void drop(DropTargetDropEvent dtde) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE); /* Sonst können keine Dateien abgelegt werden */
					final Transferable transfer=dtde.getTransferable();

					/* Datei(en) abgelegt */
					if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
						try {
							final Object obj=transfer.getTransferData(DataFlavor.javaFileListFlavor);
							if (obj instanceof List) for (Object entry: ((List<?>)obj)) if (entry instanceof File) {
								if (dropFile((File)entry)) break;
							}
							return;
						} catch (UnsupportedFlavorException | IOException e) {return;}
					}
				}
			});
		} catch (TooManyListenersException e1) {return false;}

		return true;
	}

	/**
	 * Erstellt eine neue Schaltfläche und fügt diese an {@link #toolBar} an.
	 * @param name	Name der neuen Schaltfläche
	 * @param hint	Optionaler Tooltip für die Schaltfläche
	 * @param icon	Optionales Icon für die Schaltfläche
	 * @return	Liefert die neue Schaltfläche zurück (ist bereits an {@link #toolBar} angefügt)
	 * @see #toolBar
	 */
	private JButton addButton(final String name, final String hint, final Icon icon) {
		JButton button=new JButton(name);
		if (hint!=null) button.setToolTipText(hint);
		if (icon!=null) button.setIcon(icon);
		button.addActionListener(new ButtonListener());
		toolBar.add(button);
		return button;
	}

	/**
	 * Liefert das aktuelle Bild
	 * @return	Aktuelles Bild (kann <code>null</code> sein, wenn momentan kein Bild vorhanden ist)
	 */
	public BufferedImage getImage() {
		return image;
	}

	/**
	 * Stellt das aktuelle Bild ein
	 * @param image	Neues Bild (kann <code>null</code> sein, wenn kein Bild angezeigt werden soll)
	 */
	public void setImage(final BufferedImage image) {
		this.image=image;
		imageHash=null;
		updateGUI();
		repaint();
	}

	/**
	 * Gibt an, ob das Bild ausgetauscht (neu laden / einfügen) werden darf
	 * @return	Gibt <code>true</code> zurück, wenn das Bild ausgetauscht werden darf
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Stellt ein, ob das Bild ausgetauscht (neu laden / einfügen) werden darf
	 * @param enabled	Wird hier <code>true</code> übergeben, so darf das Bild ausgetauscht werden
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		this.enabled=enabled;
		updateGUI();
	}

	/**
	 * Aktiviert oder deaktiviert die einzelnen Schaltflächen
	 * je nach dem, ob ein Bild vorhanden ist usw.
	 */
	private void updateGUI() {
		buttonCopy.setEnabled(image!=null);
		buttonPaste.setEnabled(enabled);
		buttonLoad.setEnabled(enabled);
		buttonSave.setEnabled(image!=null);
	}

	/**
	 * Reagiert auf Drag&amp;Drop einer Datei auf das Panel
	 * @param file	Datei, die übermittelt wurde
	 * @return	Gibt an, ob die Datei erfolgreich geladen werden konnte
	 */
	private boolean dropFile(final File file) {
		try {
			BufferedImage newImage=ImageIO.read(file);
			if(newImage!=null) {
				setImage(newImage);
				fireChange();
				return true;
			}
		} catch (IOException e) {return false;}
		return false;
	}

	/**
	 * Kopiert das aktuelle Bild (sofern eins vorhanden ist) in die Zwischenablage
	 */
	public void copyToClipboard() {
		if (image==null) return;
		ImageTools.copyImageToClipboard(image);
	}

	/**
	 * Versucht aus der Zwischenablage ein Bild zu laden und dieses als das aktuelle Bild zu verwenden.<br>
	 * Diese Funktion kann auch dann genutzt werden, wenn <code>setEnabled()</code> auf <code>false</code> gestellt ist.
	 * @return	Gibt <code>true</code> zurück, wenn das Bild geladen werden konnte
	 */
	public boolean pasteFromClipboard() {
		Transferable transferable=Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

		try {
			if (transferable!=null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				Image tempImage=(Image)transferable.getTransferData(DataFlavor.imageFlavor);
				if (tempImage!=null) {
					image=new BufferedImage(tempImage.getWidth(null),tempImage.getHeight(null),BufferedImage.TYPE_4BYTE_ABGR);
					image.getGraphics().drawImage(tempImage,0,0,null);
					imageHash=null;
					fireChange();
					updateGUI();
					repaint();
					return true;
				}
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}


	/**
	 * Versucht aus einer Datei ein neues Bild zu laden.<br>
	 * Diese Funktion kann auch dann genutzt werden, wenn <code>setEnabled()</code> auf <code>false</code> gestellt ist.
	 * @return	Gibt <code>true</code> zurück, wenn das Bild geladen werden konnte
	 */
	public boolean loadFromFile() {
		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Window.LoadImage"));
		FileFilter images=new FileNameExtensionFilter(Language.tr("FileType.AllImages")+" (*.jpg, *.jpeg, *.gif, *.png, *.bmp)","jpg","jpeg","gif","png","bmp");
		fc.addChoosableFileFilter(images);
		fc.setFileFilter(images);

		if (fc.showOpenDialog(this)!=JFileChooser.APPROVE_OPTION) return false;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (!file.exists()) {
			MsgBox.error(this,Language.tr("XML.LoadErrorTitle"),String.format(Language.tr("XML.FileNotFound"),file.toString()));
			return false;
		}

		try {
			BufferedImage newImage=ImageIO.read(file);
			if(newImage!=null) {
				setImage(newImage);
				fireChange();
				return true;
			}
		} catch (IOException e) {return false;}
		return false;
	}

	/**
	 * Versucht das aktuelle Bild in einer Datei zu speichern.
	 * @return	Gibt <code>true</code> zurück, wenn das Bild gespeichert werden konnte
	 */
	public boolean saveToFile() {
		if (image==null) return false;

		JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		fc.setDialogTitle(Language.tr("Window.SaveImage"));
		final FileFilter jpg=new FileNameExtensionFilter(Language.tr("FileType.jpeg")+" (*.jpg, *.jpeg)","jpg","jpeg");
		final FileFilter gif=new FileNameExtensionFilter(Language.tr("FileType.gif")+" (*.gif)","gif");
		final FileFilter png=new FileNameExtensionFilter(Language.tr("FileType.png")+" (*.png)","png");
		final FileFilter bmp=new FileNameExtensionFilter(Language.tr("FileType.bmp")+" (*.bmp)","bmp");
		final FileFilter tiff=new FileNameExtensionFilter(Language.tr("FileType.tiff")+" (*.tiff, *.tif)","tiff","tif");
		fc.addChoosableFileFilter(png);
		fc.addChoosableFileFilter(jpg);
		fc.addChoosableFileFilter(gif);
		fc.addChoosableFileFilter(bmp);
		if (ImageIOFormatCheck.hasTIFF()) fc.addChoosableFileFilter(tiff);
		fc.setFileFilter(png);
		fc.setAcceptAllFileFilterUsed(false);

		if (fc.showSaveDialog(this)!=JFileChooser.APPROVE_OPTION) return false;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();

		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==jpg) file=new File(file.getAbsoluteFile()+".jpg");
			if (fc.getFileFilter()==gif) file=new File(file.getAbsoluteFile()+".gif");
			if (fc.getFileFilter()==png) file=new File(file.getAbsoluteFile()+".png");
			if (fc.getFileFilter()==bmp) file=new File(file.getAbsoluteFile()+".bmp");
			if (fc.getFileFilter()==tiff) file=new File(file.getAbsoluteFile()+".tiff");
		}

		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(this,file)) return false;
			return false;
		}

		String s="png";
		if (file.getName().toLowerCase().endsWith(".jpg")) s="jpg";
		if (file.getName().toLowerCase().endsWith(".jpeg")) s="jpg";
		if (file.getName().toLowerCase().endsWith(".gif")) s="gif";
		if (file.getName().toLowerCase().endsWith(".bmp")) s="bmp";
		if (file.getName().toLowerCase().endsWith(".tiff")) s="tiff";
		if (file.getName().toLowerCase().endsWith(".tif")) s="tiff";
		try {return ImageIO.write(image,s,file);} catch (IOException e) {return false;}
	}

	/**
	 * Zeigt das Popupmenü zur Auswahl eines der Vorgabebilder an.
	 * @param parent	Elternkomponente zur Ausrichtung des Popupmenüs
	 */
	private void showTemplatesMenu(final Component parent) {
		JPopupMenu menu=new JPopupMenu();

		final AnimationImageSource imageSource=new AnimationImageSource();
		imageSource.addIconsToMenu(menu,name->{
			image=imageSource.get(name,modelImages,16);
			imageHash=null;
			fireChange();
			updateGUI();
			repaint();
		},modelImages);

		/* Vierspaltiges Layout */ menu.setLayout(new GridLayout((int)Math.round(Math.ceil(menu.getComponentCount()/4.0)),4));

		menu.show(parent,0,parent.getHeight());
	}

	/**
	 * Internes Panel in das das eigentliche Bild gezeichnet wird
	 */
	private class ImageHolderPanel extends JPanel {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 4214953664624697281L;

		/**
		 * Konstruktor der Klasse
		 */
		public ImageHolderPanel() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		/**
		 * Zeichnet das Bild in ein Grafik-Objekt
		 * @param graphics	Grafik-Objekt in das das Bild gezeichnet werden soll
		 */
		private void drawImage(final Graphics graphics) {
			if (image==null) return;

			final Point p=new Point(1,1);
			final Dimension s=new Dimension(getSize().width-2,getSize().height-2);

			final Object[] data=ScaledImageCache.getScaledImageCache().getScaledImage(imageHash,image,s.width,s.height);
			final BufferedImage scaledImage=(BufferedImage)data[0];
			imageHash=(String)data[1];
			graphics.drawImage(scaledImage,p.x,p.y,null);
		}

		@Override
		public void paint(Graphics graphics) {
			graphics.setClip(0,0,getWidth(),getHeight());
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0,0,getWidth()-1,getHeight()-1);
			graphics.setColor(Color.DARK_GRAY);
			graphics.drawRect(0,0,getWidth()-1,getHeight()-1);

			drawImage(graphics);
		}
	}

	/**
	 * Reagiert auf Klicks auf die verschiedenen Schaltflächen
	 */
	private class ButtonListener implements ActionListener {
		/**
		 * Konstruktor der Klasse
		 */
		public ButtonListener() {
			/*
			 * Wird nur benötigt, um einen JavaDoc-Kommentar für diesen (impliziten) Konstruktor
			 * setzen zu können, damit der JavaDoc-Compiler keine Warnung mehr ausgibt.
			 */
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			Object source=e.getSource();
			if (source==buttonCopy) {copyToClipboard(); return;}
			if (source==buttonPaste) {pasteFromClipboard(); return;}
			if (source==buttonLoad) {loadFromFile(); return;}
			if (source==buttonSave) {saveToFile(); return;}
			if (source==buttonTemplate) {showTemplatesMenu(buttonTemplate); return;}
		}
	}

	/**
	 * Listener die benachrichtigt werden, wenn ein Bild geladen wird
	 * @see #fireChange()
	 * @see #addChangeListener(ActionListener)
	 * @see #removeChangeListener(ActionListener)
	 */
	private Set<ActionListener> changeListeners=new HashSet<>();

	/**
	 * Fügt einen Listener hinzu, der benachrichtigt wird, wenn ein Bild geladen wird
	 * @param changeListener	Zu benachrichtigender Listener
	 * @return	Gibt an, ob der Listener hinzugefügt werden konnte
	 */
	public boolean addChangeListener(final ActionListener changeListener) {
		return changeListeners.add(changeListener);
	}

	/**
	 * Entfernt einen Listener aus der Liste der im Falle einer Änderung zu benachrichtigenden Listener
	 * @param clickListener	In Zukunft nicht mehr zu benachrichtigender Listener
	 * @return	Gibt <code>true</code> zurück, wenn der Listener erfolgreich aus der Liste entfernt werden konnte
	 */
	public boolean removeChangeListener(final ActionListener clickListener) {
		return changeListeners.remove(clickListener);
	}

	/**
	 * Benachrichtigt die Listener, dass ein Bild geladen wurde.
	 *  @see #changeListeners
	 *  @see #addChangeListener(ActionListener)
	 *  @see #removeChangeListener(ActionListener)
	 */
	private void fireChange() {
		final ActionEvent event=new ActionEvent(this,AWTEvent.RESERVED_ID_MAX+1,"change");
		for (ActionListener listener: changeListeners) listener.actionPerformed(event);
	}
}