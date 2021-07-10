/**
 * Copyright 2021 Alexander Herzog
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
package ui.modelproperties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import language.Language;
import mathtools.NumberTools;
import simulator.editmodel.EditModel;
import systemtools.BaseDialog;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelSurfaceAnimatorBase;

/**
 * Dialogseite "Kunden"
 * @author Alexander Herzog
 * @see ModelPropertiesDialog
 * @see ModelPropertiesDialogPage
 */
public class ModelPropertiesDialogPageClients extends ModelPropertiesDialogPage {
	/** Objekt das die verfügbaren Animations-Icons vorhält */
	private final AnimationImageSource imageSource;
	/** Datenmodell für {@link #clientColorsList} */
	private DefaultListModel<ClientRecord> clientColorsListModel;
	/** Liste mit den vorhandenen Kundentypen */
	private JList<ClientRecord> clientColorsList;

	/**
	 * Konstruktor der Klasse
	 * @param dialog	Dialog in dem sich diese Seite befindet
	 * @param model	Modell aus dem die Daten entnommen und in das die Daten geschrieben werden sollen
	 * @param readOnly	Nur-Lese-Status
	 * @param help	Hilfe-Callback
	 */
	public ModelPropertiesDialogPageClients(final ModelPropertiesDialog dialog, final EditModel model, final boolean readOnly, final Runnable help) {
		super(dialog,model,readOnly,help);
		imageSource=new AnimationImageSource();
	}

	@Override
	public void build(JPanel content) {
		clientColorsList=new JList<>(clientColorsListModel=new DefaultListModel<>());
		clientColorsList.setCellRenderer(new ElementListCellRenderer());
		content.add(new JScrollPane(clientColorsList));

		clientColorsList.addMouseListener(new MouseAdapter() {
			@Override public void mousePressed(MouseEvent e) {if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {editSelectedClientColor(); e.consume(); return;}}
		});
		clientColorsList.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {if (e.getKeyCode()==KeyEvent.VK_ENTER) {editSelectedClientColor(); e.consume(); return;}}
		});

		updateClientDataList();
	}

	/**
	 * Ruft den Dialog zum Bearbeiten der spezifischen Einstellungen
	 * für den aktuell gewählten Kundentyp auf.
	 * @see #editClientData(Component, Runnable, EditModel, String, boolean)
	 */
	private void editSelectedClientColor() {
		if (readOnly) return;

		final int selected=clientColorsList.getSelectedIndex();
		if (selected<0) return;
		final String clientType=model.surface.getClientTypes().get(selected);

		if (editClientData(dialog,help,model,clientType,false)) updateClientDataList();
	}

	/**
	 * Ruft den Dialog zum Bearbeiten der spezifischen Einstellungen für einen Kundentyp auf
	 * @param owner	Übergeordnetes Element
	 * @param help	Hilfe-Runnable
	 * @param model	Modell, welches weitere Icondaten vorhält, aus dem die Kundendaten ausgelesen werden und in das die Kundendaten evtl. auch wieder zurückgeschrieben werden
	 * @param clientType	Name des zu bearbeitenden Kundentyps
	 * @param readOnly	Nur-Lese-Status für Dialog
	 * @return	Gibt <code>true</code> zurück, wenn der Dialog per Ok geschlossen wurde (und die Daten im Modell aktualisiert wurden)
	 */
	public static boolean editClientData(final Component owner, final Runnable help, final EditModel model, final String clientType, final boolean readOnly) {
		final Color color=model.clientData.getColor(clientType);
		String icon=model.clientData.getIcon(clientType);
		if (icon==null || icon.trim().isEmpty()) icon=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
		final double[] costs=model.clientData.getCosts(clientType);

		final ClientDataDialog dialog=new ClientDataDialog(owner,help,color,icon,costs,model.animationImages,readOnly);
		dialog.setVisible(true);
		if (dialog.getClosedBy()!=BaseDialog.CLOSED_BY_OK) return false;

		if (dialog.getUserColor()==null) model.clientData.delete(clientType); else model.clientData.setColor(clientType,dialog.getUserColor());
		model.clientData.setIcon(clientType,dialog.getIcon());
		if (dialog.getCosts()==null) model.clientData.delete(clientType); else model.clientData.setCosts(clientType,dialog.getCosts());

		return true;
	}

	/**
	 * Aktualisiert die Liste der Kundentypen.
	 * @see #editClientData(Component, Runnable, EditModel, String, boolean)
	 */
	private void updateClientDataList() {
		final int selected=clientColorsList.getSelectedIndex();
		clientColorsListModel.clear();
		final List<String> clientTypes=model.surface.getClientTypes();
		if (clientTypes.size()>0) clientColorsList.setPrototypeCellValue(new ClientRecord(clientTypes.get(0)));
		for (String clientType: clientTypes) clientColorsListModel.addElement(new ClientRecord(clientType));
		clientColorsList.setSelectedIndex(selected);
	}

	/**
	 * Datensatz für einen Eintrag in {@link #clientColorsListModel}.<br>
	 * Die Labels für die Einträge werden durch diese Abstraktionsschicht erst bei Bedarf erstellt.
	 */
	private class ClientRecord {
		/** Name des Kundentyps */
		private final String name;
		/** Label zur Darstellung in der Liste */
		private JLabel label;

		/**
		 * Konstruktor der Klasse
		 * @param name	Name des Kundentyps
		 */
		public ClientRecord(final String name) {
			this.name=name;
		}

		/**
		 * Generiert oder liefert das bereits generierte Label
		 * zur Darstellung der Kundentypdaten in der Liste
		 * @return	Label für den Listeneintrag
		 */
		public JLabel getJLabel() {
			if (label==null) label=getLabel(name);
			return label;
		}
	}

	/**
	 * Zeichnet ein farbiges Rechteck in ein Grafikobjekt
	 * (um zu verdeutlichen, welche Farbe für einen Kundentyp gelten soll)
	 * @param g	Ausgabe-Grafikobjekt
	 * @param color	Zu verwendende Farbe
	 * @param x	x-Position des Kastens
	 * @param y	y-Position des Kastens
	 * @see #getLabel(String)
	 */
	private void drawColorToImage(final Graphics g, final Color color, final int x, final int y) {
		if (color==null) {
			g.setColor(Color.BLACK);
			g.drawLine(x+0,y+0,x+31,y+31);
			g.drawLine(x+31,y+0,x+0,y+31);
		} else {
			g.setColor(color);
			g.fillRect(x+0,y+0,31,31);
		}
		g.setColor(Color.BLACK);
		g.drawRect(x+0,y+0,31,31);
	}

	/**
	 * Erstellt ein {@link JLabel}-Element für die {@link #clientColorsList}
	 * zur Darstellung der Daten eines Kundentyps.
	 * @param clientType	Kundentyp dessen Daten dargestellt werden solle
	 * @return	{@link JLabel} das die Daten für die Listendarstellung enthält
	 * @see #updateClientDataList()
	 */
	private JLabel getLabel(final String clientType) {
		final Color color=model.clientData.getColor(clientType);
		String iconName=model.clientData.getIcon(clientType);
		if (iconName==null || iconName.trim().isEmpty()) iconName=ModelSurfaceAnimatorBase.DEFAULT_CLIENT_ICON_NAME;
		final double[] costs=model.clientData.getCosts(clientType);

		/* Text aufbauen */
		final StringBuilder sb=new StringBuilder();
		sb.append("<b><span style=\"font-size: larger;\">"+clientType+"</span></b><br>");
		if (color==null) sb.append(Language.tr("Editor.Dialog.Tab.Clients.Color.Automatic")); else sb.append(Language.tr("Editor.Dialog.Tab.Clients.Color.UserDefined"));

		if (costs[0]!=0 || costs[1]!=0 || costs[2]!=0) {
			sb.append("<br>");
			sb.append(String.format(Language.tr("Editor.Dialog.Tab.Clients.Costs"),NumberTools.formatNumber(costs[0]),NumberTools.formatNumber(costs[1]),NumberTools.formatNumber(costs[2])));
		}

		/* Bild aufbauen */
		final BufferedImage image=new BufferedImage(64,32,BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics g=image.getGraphics();

		final Image symbol=imageSource.get(iconName,model.animationImages,32);
		g.drawImage(symbol,0,0,null);
		drawColorToImage(g,color,32,0);
		final Icon icon=new ImageIcon(image);

		/* Label erstellen */
		final JLabel label=new JLabel("<html><body>"+sb.toString()+"</body></html>");
		label.setIcon(icon);
		return label;
	}

	/**
	 * Renderer für die Kundentypenliste
	 */
	private class ElementListCellRenderer extends DefaultListCellRenderer {
		/**
		 * Serialisierungs-ID der Klasse
		 * @see Serializable
		 */
		private static final long serialVersionUID = 6913560392242517601L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,boolean isSelected, boolean cellHasFocus) {
			Component renderer=super.getListCellRendererComponent(list,value, index, isSelected, cellHasFocus);
			if (value instanceof JLabel) {
				((ElementListCellRenderer)renderer).setText(((JLabel)value).getText());
				((ElementListCellRenderer)renderer).setIcon(((JLabel)value).getIcon());
			}
			if (value instanceof ClientRecord) {
				renderer=((ClientRecord)value).getJLabel();
			}
			return renderer;
		}
	}

	@Override
	public void storeData() {
		/* Kundendaten werden direkt im <code>model</code>-Element bearbeitet, da dieses eine Kopie ist und am Ende ggf. per <code>getModel</code> in den Editor geladen wird. */
	}
}
