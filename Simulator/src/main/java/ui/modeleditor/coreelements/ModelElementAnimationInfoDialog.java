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
package ui.modeleditor.coreelements;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import language.Language;
import mathtools.TimeTools;
import mathtools.distribution.swing.CommonVariables;
import simulator.runmodel.RunDataClient;
import simulator.runmodel.RunModel;
import systemtools.BaseDialog;
import systemtools.MsgBox;
import ui.help.Help;
import ui.images.Images;
import ui.modeleditor.AnimationImageSource;
import ui.modeleditor.ModelAnimationImages;

/**
 * Zeigt aktuelle Animations-/Simulationsdaten zu einer bestimmten Station an
 * @author Alexander Herzog
 * @see ModelElement
 */
public class ModelElementAnimationInfoDialog extends BaseDialog {
	/**
	 * Serialisierungs-ID der Klasse
	 * @see Serializable
	 */
	private static final long serialVersionUID = 2428681388058860543L;

	/** Timer für automatische Aktualisierungen */
	private Timer timer;
	/** Simulationsmodell mit Informationen zu den Stationen usw. */
	private final RunModel model;
	/** Anzuzeigender Text im Content-Bereich */
	private final Supplier<String> info;
	/** Liste mit an der Station wartenden Kunden abrufen (kann <code>null</code> sein) */
	private final Supplier<List<ClientInfo>> clientWaitingInfo;
	/** Liste mit an der Station befindlichen Kunden abrufen (kann <code>null</code> sein) */
	private final Supplier<List<ClientInfo>> clientInfo;

	/** Schaltfläche zum Umschalten zwischen automatischer und manueller Aktualisierung */
	private final JButton buttonAutoUpdate;
	/** Schaltfläche zum Aktualisierung der Kundenliste */
	private final JButton buttonUpdateClients;

	/** Objekt welches die Icons für die Animation vorhält */
	private final AnimationImageSource images;

	/** Text-Ausgabebereich */
	private final JTextArea textArea;

	/** Liste der wartenden Kunden */
	private final JList<JLabel> listWaiting;
	/** Datenmodell für die Liste der wartenden Kunden */
	private final DefaultListModel<JLabel> listWaitingModel;
	/** Liste der Datensätze der wartenden Kunden */
	private List<ClientInfo> listWaitingData;

	/** Liste der Kunden */
	private final JList<JLabel> listAll;
	/** Datenmodell für die Liste der Kunden */
	private final DefaultListModel<JLabel> listAllModel;
	/** Liste der Datensätze der Kunden */
	private List<ClientInfo> listAllData;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationInfoDialog</code>
	 * @param owner	Übergeordnetes Fenster
	 * @param title	Anzuzeigender Titel
	 * @param model	Simulationsmodell mit Informationen zu den Stationen usw.
	 * @param info	Anzuzeigender Text im Content-Bereich
	 * @param clientWaitingInfo	Optionale Liste mit an der Station wartenden Kunden (kann <code>null</code> sein)
	 * @param clientInfo	Optionale Liste mit an der Station befindlichen Kunden (kann <code>null</code> sein)
	 */
	public ModelElementAnimationInfoDialog(final Component owner, final String title, final RunModel model, final Supplier<String> info, final Supplier<List<ClientInfo>> clientWaitingInfo, final Supplier<List<ClientInfo>> clientInfo) {
		super(owner,title);
		this.model=model;
		this.info=info;
		this.clientWaitingInfo=clientWaitingInfo;
		this.clientInfo=clientInfo;

		images=new AnimationImageSource();
		timer=null;

		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(ModelElementAnimationInfoDialog.this.owner,"AnimationStatistics"));
		content.setLayout(new BorderLayout());

		/* Größe für Listeneinträge */
		final JLabel defaultSizeLabel=new JLabel("<html><body>Line1<br>Line2<br>Line3</body></html>");
		defaultSizeLabel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

		/* Toolbar */
		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		addButton(toolbar,Language.tr("Dialog.Button.Copy"),Images.EDIT_COPY.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.CopyHint"),e->commandCopy());
		addButton(toolbar,Language.tr("Dialog.Button.Save"),Images.GENERAL_SAVE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveHint"),e->commandSave());
		toolbar.addSeparator();
		addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Update"),Images.ANIMATION_DATA_UPDATE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateHint"),e->commandUpdate());
		buttonAutoUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdate"),Images.ANIMATION_DATA_UPDATE_AUTO.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdateHint"),e->commandAutoUpdate());
		buttonUpdateClients=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateClients"),Images.MODELPROPERTIES_CLIENTS.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateClientsHint"),e->commandUpdateClientList());
		buttonUpdateClients.setVisible(false);

		/* Tabs */
		final JTabbedPane tabs=new JTabbedPane();
		content.add(tabs,BorderLayout.CENTER);

		JPanel tab;
		JPanel line;

		/* Text-Datenfeld */
		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.Data"),tab=new JPanel(new BorderLayout()));
		textArea=new JTextArea(info.get());
		tab.add(new JScrollPane(textArea),BorderLayout.CENTER);
		textArea.setEditable(false);

		/* Warteschlange */
		if (clientWaitingInfo==null) {
			listWaiting=null;
			listWaitingModel=null;
		} else {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients"),tab=new JPanel(new BorderLayout()));
			tab.add(new JScrollPane(listWaiting=new JList<>(listWaitingModel=new DefaultListModel<>())),BorderLayout.CENTER);
			listWaiting.setCellRenderer(new JLabelRender());
			listWaiting.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) commandShowWaitingClientData();
				}
			});
			listWaiting.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(final KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_ENTER) {
						commandShowWaitingClientData();
						e.consume();
						return;
					}
				}
			});
			listWaiting.setPrototypeCellValue(defaultSizeLabel);
			commandUpdateWaitingClientListOnly();
		}

		/* Allgemeine Kundenliste */
		if (clientInfo==null) {
			listAll=null;
			listAllModel=null;
		} else {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.AllClients"),tab=new JPanel(new BorderLayout()));
			tab.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.NORTH);
			line.add(new JLabel("<html><body>"+Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.AllClients.Info")+"</body></html>"));
			tab.add(new JScrollPane(listAll=new JList<>(listAllModel=new DefaultListModel<>())),BorderLayout.CENTER);
			listAll.setCellRenderer(new JLabelRender());
			listAll.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) commandShowClientData();
				}
			});
			listAll.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(final KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_ENTER) {
						commandShowClientData();
						e.consume();
						return;
					}
				}
			});
			listAll.setPrototypeCellValue(defaultSizeLabel);
		}

		/* Icons auf den Tabs */
		int nr=0;
		tabs.setIconAt(nr++,Images.GENERAL_TABLE.getIcon());
		if (clientWaitingInfo!=null) tabs.setIconAt(nr++,Images.EXTRAS_QUEUE.getIcon());
		if (clientInfo!=null) tabs.setIconAt(nr++,Images.MODELPROPERTIES_CLIENTS.getIcon());

		/* Dialog starten */
		tabs.addChangeListener(e->buttonUpdateClients.setVisible(this.clientInfo!=null && tabs.getSelectedIndex()==tabs.getTabCount()-1));
		setMinSizeRespectingScreensize(600,400);
		setSizeRespectingScreensize(800,600);
		setLocationRelativeTo(this.owner);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * Erstellt eine neue Schaltfläche und fügt sie zur Symbolleiste hinzu.
	 * @param toolbar	Symbolleiste auf der die neue Schaltfläche eingefügt werden soll
	 * @param name	Beschriftung der Schaltfläche
	 * @param hint	Tooltip für die Schaltfläche (darf <code>null</code> sein)
	 * @param icon	Optionales Icon für die Schaltfläche (darf <code>null</code> sein)
	 * @param listener	Aktion die beim Anklicken der Schaltfläche ausgeführt werden soll
	 * @return	Neue Schaltfläche (ist bereits in die Symbolleiste eingefügt)
	 */
	private JButton addButton(final JToolBar toolbar, final String name, final Icon icon, final String hint, final ActionListener listener) {
		final JButton button=new JButton(name);
		if (icon!=null) button.setIcon(icon);
		if (hint!=null && !hint.trim().isEmpty()) button.setToolTipText(hint);
		button.addActionListener(listener);
		toolbar.add(button);
		return button;
	}

	/**
	 * Befehl: Angezeigte Daten in die Zwischenablage kopieren
	 */
	private void commandCopy() {
		if (buttonAutoUpdate.isSelected()) commandAutoUpdate();

		getToolkit().getSystemClipboard().setContents(new StringSelection(textArea.getText()),null);
	}

	/**
	 * Befehl: Angezeigte Daten als Text speichern
	 */
	private void commandSave() {
		if (buttonAutoUpdate.isSelected()) commandAutoUpdate();

		final JFileChooser fc=new JFileChooser();
		CommonVariables.initialDirectoryToJFileChooser(fc);
		FileFilter filter;
		fc.setDialogTitle(Language.tr("FileType.Save.Text"));
		filter=new FileNameExtensionFilter(Language.tr("FileType.Text")+" (*.txt)","txt");
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		fc.setAcceptAllFileFilterUsed(false);
		if (fc.showSaveDialog(owner)!=JFileChooser.APPROVE_OPTION) return;
		CommonVariables.initialDirectoryFromJFileChooser(fc);
		File file=fc.getSelectedFile();
		if (file.getName().indexOf('.')<0) {
			if (fc.getFileFilter()==filter) file=new File(file.getAbsoluteFile()+".txt");
		}
		if (file.exists()) {
			if (!MsgBox.confirmOverwrite(owner,file)) return;
		}

		try {
			if (file.isFile()) {
				if (!file.delete()) {
					MsgBox.error(ModelElementAnimationInfoDialog.this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Title"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Info"),file.toString()));
					return;
				}
			}
			Files.write(Paths.get(file.toURI()),textArea.getText().getBytes(),StandardOpenOption.CREATE_NEW);
		} catch (IOException e1) {
			MsgBox.error(ModelElementAnimationInfoDialog.this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Title"),String.format(Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveError.Info"),file.toString()));
		}
	}

	/**
	 * Befehl: Anzeige aktualisieren
	 */
	private void commandUpdate() {
		/* Text-Datenfeld */
		textArea.setText(info.get());
		textArea.setSelectionStart(0);
		textArea.setSelectionEnd(0);

		/* Kundenliste */
		commandUpdateWaitingClientListOnly();
	}

	/**
	 * Befehl: Liste der wartenden Kunden aktualisieren
	 */
	private void commandUpdateWaitingClientListOnly() {
		if (clientWaitingInfo==null) return;

		listWaitingData=clientWaitingInfo.get();
		listWaitingModel.clear();
		if (listWaitingData!=null) listWaitingData.stream().map(clientInfo->clientInfo.buildLabel(images,true)).forEach(listWaitingModel::addElement);
	}

	/**
	 * Befehl: Liste der Kunden aktualisieren
	 */
	private void commandUpdateClientList() {
		if (clientInfo==null) return;

		listAllData=clientInfo.get();
		listAllModel.clear();
		if (listAllData!=null) listAllData.stream().map(clientInfo->clientInfo.buildLabel(images,false)).forEach(listAllModel::addElement);
	}

	/**
	 * Timer-Task zur Akualisierung der Daten
	 * @see ModelElementAnimationInfoDialog#commandAutoUpdate()
	 */
	private class UpdateTimerTask extends TimerTask {
		@Override
		public void run() {
			if (buttonAutoUpdate.isSelected()) SwingUtilities.invokeLater(()->{
				commandUpdate();
				scheduleNextUpdate();
			});
		}
	}

	/**
	 * Befehl: Anzeige automatisch aktualisieren (an/aus)
	 */
	private void commandAutoUpdate() {
		buttonAutoUpdate.setSelected(!buttonAutoUpdate.isSelected());

		if (buttonAutoUpdate.isSelected()) {
			timer=new Timer("SimulationDataUpdate");
			scheduleNextUpdate();
		} else {
			if (timer!=null) {timer.cancel(); timer=null;}
		}
	}

	/**
	 * Plant den nächsten Update-Schritt ein.
	 * @see #commandAutoUpdate()
	 * @see UpdateTimerTask
	 */
	private void scheduleNextUpdate() {
		if (timer!=null) timer.schedule(new UpdateTimerTask(),250);
	}

	/**
	 * Befehl: Daten zu dem gewählten wartenden Kunden anzeigen
	 */
	private void commandShowWaitingClientData() {
		final int index=listWaiting.getSelectedIndex();
		if (index<0) return;
		if (buttonAutoUpdate.isSelected()) commandAutoUpdate();

		final ClientInfo clientInfo=listWaitingData.get(index);
		new ModelElementAnimationInfoClientDialog(this,model,clientInfo,true);
	}

	/**
	 * Befehl: Daten zu dem gewählten Kunden anzeigen
	 */
	private void commandShowClientData() {
		final int index=listAll.getSelectedIndex();
		if (index<0) return;

		final ClientInfo clientInfo=listAllData.get(index);
		new ModelElementAnimationInfoClientDialog(this,model,clientInfo,false);
	}

	@Override
	protected boolean closeButtonOK() {
		if (timer!=null) {timer.cancel(); timer=null;}
		return true;
	}

	/**
	 * Datensatz für die Informationen zu einem Kunden
	 */
	public static class ClientInfo {
		/** Benutzerdefinierte Animationsicons */
		private final ModelAnimationImages animationImages;

		/** Fortlaufende Nummer des Kunden */
		public final long number;
		/** Nummer des Kundentyps */
		public final int typeId;
		/** Name des Kundentyps */
		public final String typeName;
		/** Icon für den Kunden */
		public final String icon;
		/** Wurde dieser Kunde in der Warm-up-Phase erstellt? */
		public boolean isWarmUp;
		/** Sollen die Daten zu diesem Kunden in der Statistik erfasst werden? */
		public boolean inStatistics;
		/** Anzahl an Kunden in dem Batch (0, wenn dieser Kunde kein temporärer Batch ist) */
		public final int batch;
		/** ID der Station an der sich der Kunde momentan befindet */
		public final int currentPosition;
		/** Bisherige Wartezeit des Kunden */
		public final double waitingTime;
		/** Bisherige Transferzeit des Kunden */
		public final double transferTime;
		/** Bisherige Bedienzeit des Kunden */
		public final double processTime;
		/** Bisherige Verweilzeit des Kunden */
		public final double residenceTime;
		/** Wartezeit-Kosten */
		public final double waitingCosts;
		/** Transferzeit-Kosten */
		public final double transferCosts;
		/** Bedienzeit-Kosten */
		public final double processCosts;
		/** Numerische Kundendatenfelder */
		public final Map<Integer,Double> clientData;
		/** Text-basierte Kundendatenfelder */
		public final Map<String,String> clientTextData;
		/** Aufgezeichneter Pfad (kann <code>null</code> sein, wenn kein Pfad aufgezeichnet wurde) */
		public final int[] path;

		/**
		 * Konstruktor der Klasse
		 * @param animationImages	Benutzerdefinierte Animationsicons
		 * @param model	Statisches Simulationsdatenmodell
		 * @param client	Kundendatenobjekt dem die Informationen entnommen werden sollen
		 */
		public ClientInfo(final ModelAnimationImages animationImages, final RunModel model, final RunDataClient client)  {
			this.animationImages=animationImages;

			number=client.clientNumber;

			typeId=client.type;
			typeName=model.clientTypes[client.type];
			icon=client.icon;

			isWarmUp=client.isWarmUp;
			inStatistics=client.inStatistics;

			final List<RunDataClient> batchClients=client.getBatchData();
			if (batchClients==null) batch=0; else batch=batchClients.size();

			currentPosition=client.nextStationID;

			waitingTime=client.waitingTime;
			transferTime=client.transferTime;
			processTime=client.processTime;
			residenceTime=client.residenceTime;

			waitingCosts=client.waitingAdditionalCosts;
			transferCosts=client.transferAdditionalCosts;
			processCosts=client.processAdditionalCosts;

			clientData=new HashMap<>();
			for (int i=0;i<client.getMaxUserDataIndex();i++) {
				final double value=client.getUserData(i);
				if (value!=0.0) clientData.put(i,value);
			}

			clientTextData=new HashMap<>();
			for (String key: client.getUserDataStringKeys()) clientTextData.put(key,client.getUserDataString(key));

			path=client.getPath();
		}

		/**
		 * Erzeugt ein Label zur Anzeige in der Liste zu diesem Kundeninfo-Datensatz
		 * @param images	Objekt welches die Icons für die Animation vorhält
		 * @param isWaitingClientsList	Handelt es sich um einen noch wartenden Kunden?
		 * @return	Label zur Anzeige in der Liste
		 */
		public JLabel buildLabel(final AnimationImageSource images, final boolean isWaitingClientsList) {
			final StringBuilder text=new StringBuilder();
			text.append("<html><body>");

			text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.RunningNumber"));
			text.append(": <b>");
			text.append(number);
			text.append("</b>, ");
			text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.ClientType"));
			text.append(": <b>");
			text.append(typeName);
			text.append("</b> (id=");
			text.append(typeId);
			text.append(")<br>");
			text.append("w=");
			text.append(TimeTools.formatExactTime(waitingTime));
			text.append(", t=");
			text.append(TimeTools.formatExactTime(transferTime));
			text.append(", p=");
			text.append(TimeTools.formatExactTime(processTime));
			text.append(", v=");
			text.append(TimeTools.formatExactTime(residenceTime));
			if (isWaitingClientsList) {
				text.append(" (");
				text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.OnArrivalAtStation"));
				text.append(")");
			}
			text.append("<br>");

			text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Number")+": ");
			text.append(clientData.size());
			text.append(", ");
			text.append(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Field.Text")+": ");
			text.append(clientTextData.size());

			text.append("</body></html>");

			final JLabel label=new JLabel(text.toString(),getIcon(images),SwingConstants.LEADING);
			label.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));
			return label;
		}

		/**
		 * Liefert das Icon für den Kunden.
		 * @param images	Objekt welches die Icons für die Animation vorhält
		 * @return	Icon für den Kunden
		 */
		public Icon getIcon(final AnimationImageSource images) {
			final BufferedImage image=images.get(icon,animationImages,24,1);
			return (image==null)?null:new ImageIcon(image);
		}

		/**
		 * Wandelt eine Liste mit Simulationsdaten-Kunden in Kundeninfo-Objekte um
		 * @param animationImages	Benutzerdefinierte Animationsicons
		 * @param model	Statisches Simulationsdatenmodell
		 * @param clients	Liste der umzuwandelnden Simulationsdaten-Kunden
		 * @return	Liste mit Kundeninfo-Objekten
		 */
		public static List<ClientInfo> getList(final ModelAnimationImages animationImages, final RunModel model, final List<RunDataClient> clients) {
			if (clients==null) return new ArrayList<>();
			return clients.stream().filter(client->client!=null).map(client->new ClientInfo(animationImages,model,client)).collect(Collectors.toList());
		}
	}

	/**
	 * Renderer für die Einträge der Liste der wartenden Kunden {@link #list()}
	 */
	private static class JLabelRender implements ListCellRenderer<JLabel> {
		@Override
		public Component getListCellRendererComponent(JList<? extends JLabel> list, JLabel value, int index, boolean isSelected, boolean cellHasFocus) {
			if (isSelected) {
				value.setBackground(list.getSelectionBackground());
				value.setForeground(list.getSelectionForeground());
				value.setOpaque(true);
			} else {
				value.setBackground(list.getBackground());
				value.setForeground(list.getForeground());
				value.setOpaque(false);
			}
			return value;
		}
	}
}