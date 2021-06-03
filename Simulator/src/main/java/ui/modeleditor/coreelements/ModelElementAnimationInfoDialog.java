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
import java.awt.event.InputEvent;
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
import java.util.function.Function;
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

	/** Timer f�r automatische Aktualisierungen */
	private Timer timer;
	/** Simulationsmodell mit Informationen zu den Stationen usw. */
	private final RunModel model;
	/** Benutzerdefinierte Animationsicons */
	private final ModelAnimationImages modelImages;

	/** Tabs �ber den Bereichen */
	private final JTabbedPane tabs;
	/** Anzuzeigender Text im Content-Bereich */
	private final Supplier<String> info;
	/** Liste mit an der Station wartenden Kunden abrufen (kann <code>null</code> sein) */
	private final Supplier<List<ClientInfo>> clientWaitingInfo;
	/** Liste mit an der Station befindlichen Kunden abrufen (kann <code>null</code> sein) */
	private final Supplier<List<ClientInfo>> clientInfo;
	/** Erm�glicht das Abrufen eines tats�chlichen Kunden-Objekts (kann <code>null</code> sein, wenn kein Schreibzugriff auf die realen Kunden m�glich ist) */
	private final Function<Long,RunDataClient> getRealClient;
	/** Schaltfl�che "Kopieren" */
	private final JButton buttonCopy;
	/** Schaltfl�che "Speichern" */
	private final JButton buttonSave;
	/** Schaltfl�che "Aktualisieren" */
	private final JButton buttonUpdate;

	/** Schaltfl�che zum Umschalten zwischen automatischer und manueller Aktualisierung */
	private final JButton buttonAutoUpdate;
	/** Schaltfl�che zum Aktualisierung der Kundenliste */
	private final JButton buttonUpdateClients;

	/** Objekt welches die Icons f�r die Animation vorh�lt */
	private final AnimationImageSource images;

	/** Text-Ausgabebereich */
	private final JTextArea textArea;

	/** Liste der wartenden Kunden */
	private final JList<ClientInfo> listWaiting;
	/** Infozeile zu {@link #listWaiting} */
	private final JLabel listWaitingInfo;

	/** Liste der Kunden */
	private final JList<ClientInfo> listAll;
	/** InfoZeile zu {@link #listAll} */
	private final JLabel listAllInfo;

	/** Handelt es sich um den ersten Aufruf des "Alle Kunden"-Tabs? */
	private boolean firstAllClientsTabCall=true;

	/**
	 * Konstruktor der Klasse <code>ModelElementAnimationInfoDialog</code>
	 * @param owner	�bergeordnetes Fenster
	 * @param title	Anzuzeigender Titel	 *
	 * @param model	Simulationsmodell mit Informationen zu den Stationen usw.
	 * @param modelImages	Benutzerdefinierte Animationsicons
	 * @param info	Anzuzeigender Text im Content-Bereich
	 * @param clientWaitingInfo	Optionale Liste mit an der Station wartenden Kunden (kann <code>null</code> sein)
	 * @param clientInfo	Optionale Liste mit an der Station befindlichen Kunden (kann <code>null</code> sein)
	 * @param getRealClient	Erm�glicht das Abrufen eines tats�chlichen Kunden-Objekts (kann <code>null</code> sein, wenn kein Schreibzugriff auf die realen Kunden m�glich ist)
	 */
	@SuppressWarnings("unchecked")
	public ModelElementAnimationInfoDialog(final Component owner, final String title, final RunModel model, final ModelAnimationImages modelImages, final Supplier<String> info, final Supplier<List<ClientInfo>> clientWaitingInfo, final Supplier<List<ClientInfo>> clientInfo, final Function<Long,RunDataClient> getRealClient) {
		super(owner,title);
		this.model=model;
		this.modelImages=modelImages;
		this.info=info;
		this.clientWaitingInfo=clientWaitingInfo;
		this.clientInfo=clientInfo;
		this.getRealClient=getRealClient;

		images=new AnimationImageSource();
		timer=null;

		showCloseButton=true;
		final JPanel content=createGUI(()->Help.topicModal(ModelElementAnimationInfoDialog.this.owner,"AnimationStatistics"));
		content.setLayout(new BorderLayout());

		/* Gr��e f�r Listeneintr�ge */
		final JLabel defaultSizeLabel=new JLabel("<html><body>Line1<br>Line2<br>Line3</body></html>");
		defaultSizeLabel.setBorder(BorderFactory.createEmptyBorder(5,10,5,10));

		/* Toolbar */
		final JToolBar toolbar=new JToolBar();
		toolbar.setFloatable(false);
		content.add(toolbar,BorderLayout.NORTH);
		buttonCopy=addButton(toolbar,Language.tr("Dialog.Button.Copy"),Images.EDIT_COPY.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.CopyHint"),e->commandCopy());
		buttonSave=addButton(toolbar,Language.tr("Dialog.Button.Save"),Images.GENERAL_SAVE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.SaveHint"),e->commandSave());
		buttonUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.Update"),Images.ANIMATION_DATA_UPDATE.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateHint"),e->commandUpdate());
		buttonAutoUpdate=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdate"),Images.ANIMATION_DATA_UPDATE_AUTO.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.AutoUpdateHint"),e->commandAutoUpdate());
		buttonUpdateClients=addButton(toolbar,Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateClients"),Images.MODELPROPERTIES_CLIENTS.getIcon(),Language.tr("Surface.PopupMenu.SimulationStatisticsData.UpdateClientsHint"),e->commandUpdateClientList());
		buttonUpdateClients.setVisible(false);

		/* Tabs */
		content.add(tabs=new JTabbedPane(),BorderLayout.CENTER);

		JPanel tab;
		Object[] data;

		/* Text-Datenfeld */
		tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.Data"),tab=new JPanel(new BorderLayout()));
		textArea=new JTextArea(info.get());
		tab.add(new JScrollPane(textArea),BorderLayout.CENTER);
		textArea.setEditable(false);

		/* Warteschlange */
		if (clientWaitingInfo==null) {
			listWaiting=null;
			listWaitingInfo=null;
		} else {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients"),tab=new JPanel(new BorderLayout()));
			data=buildList(tab,true);
			listWaiting=(JList<ClientInfo>)data[0];
			listWaitingInfo=(JLabel)data[1];
			commandUpdateWaitingClientList();
		}

		/* Allgemeine Kundenliste */
		if (clientInfo==null) {
			listAll=null;
			listAllInfo=null;
		} else {
			tabs.addTab(Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.AllClients"),tab=new JPanel(new BorderLayout()));
			data=buildList(tab,false);
			listAll=(JList<ClientInfo>)data[0];
			listAllInfo=(JLabel)data[1];
		}

		/* Icons auf den Tabs */
		int nr=0;
		tabs.setIconAt(nr++,Images.GENERAL_TABLE.getIcon());
		if (clientWaitingInfo!=null) tabs.setIconAt(nr++,Images.EXTRAS_QUEUE.getIcon());
		if (clientInfo!=null) tabs.setIconAt(nr++,Images.MODELPROPERTIES_CLIENTS.getIcon());

		/* Dialog starten */
		tabs.addChangeListener(e->{
			final int index=tabs.getSelectedIndex();
			final boolean isAllClientsTab=(this.clientInfo!=null && index==tabs.getTabCount()-1);
			buttonCopy.setVisible(index==0);
			buttonSave.setVisible(index==0);
			buttonUpdate.setVisible(!isAllClientsTab);
			buttonAutoUpdate.setVisible(!isAllClientsTab);
			buttonUpdateClients.setVisible(isAllClientsTab);
			if (isAllClientsTab && getRealClient!=null && firstAllClientsTabCall) {
				firstAllClientsTabCall=false;
				commandUpdateClientList();
			}
		});
		setMinSizeRespectingScreensize(600,400);
		setSizeRespectingScreensize(800,600);
		setLocationRelativeTo(this.owner);
		setResizable(true);
		setVisible(true);
	}

	/**
	 * F�gt eine Kundenliste in ein Panel ein
	 * @param parent	�bergeordnetes Panel
	 * @param isWaitingClientsList	Handelt es sich um wartende Kunden?
	 * @return	2-elementiges Array aus Liste und aus Label mit weiteren Informationen unter der Liste
	 */
	private Object[] buildList(final JPanel parent, final boolean isWaitingClientsList) {
		/* Neue Liste */
		final JList<ClientInfo> list;
		final JLabel info;

		/* Anlegen und einf�gen */
		parent.add(new JScrollPane(list=new JList<>(new DefaultListModel<ClientInfo>())),BorderLayout.CENTER);
		list.setCellRenderer(new JClientInfoRender(images,isWaitingClientsList));
		list.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e)) {
					if (getRealClient!=null && e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK) {
						commandEditClientData(list);
					} else {
						commandShowClientData(list);
					}
				}
			}
		});
		list.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode()==KeyEvent.VK_ENTER) {
					if (getRealClient!=null && e.getModifiersEx()==InputEvent.CTRL_DOWN_MASK) {
						commandEditClientData(list);
					} else {
						commandShowClientData(list);
					}
					e.consume();
					return;
				}
			}
		});
		list.setPrototypeCellValue(new ClientInfo(null,null,new RunDataClient(0,false,0)));
		final JPanel line;
		parent.add(line=new JPanel(new FlowLayout(FlowLayout.LEFT)),BorderLayout.SOUTH);
		line.add(info=new JLabel());

		return new Object[] {list,info};
	}

	/**
	 * Erstellt eine neue Schaltfl�che und f�gt sie zur Symbolleiste hinzu.
	 * @param toolbar	Symbolleiste auf der die neue Schaltfl�che eingef�gt werden soll
	 * @param name	Beschriftung der Schaltfl�che
	 * @param hint	Tooltip f�r die Schaltfl�che (darf <code>null</code> sein)
	 * @param icon	Optionales Icon f�r die Schaltfl�che (darf <code>null</code> sein)
	 * @param listener	Aktion die beim Anklicken der Schaltfl�che ausgef�hrt werden soll
	 * @return	Neue Schaltfl�che (ist bereits in die Symbolleiste eingef�gt)
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
		commandUpdateWaitingClientList();
	}

	/**
	 * Befehl: Liste der wartenden Kunden aktualisieren
	 */
	private void commandUpdateWaitingClientList() {
		if (clientWaitingInfo==null) return;

		final int index=listWaiting.getSelectedIndex();

		final DefaultListModel<ClientInfo> model=new DefaultListModel<>();
		new Thread(()->{
			final List<ClientInfo> list=clientWaitingInfo.get();
			if (list!=null) list.forEach(model::addElement);
			SwingUtilities.invokeLater(()->{
				listWaiting.setModel(model);
				if (index>=0 && index<model.size()) listWaiting.setSelectedIndex(index);

				if (listWaitingInfo!=null) {
					final int size=model.size();
					listWaitingInfo.setText(String.format((size==1)?Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Info.Singular"):Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.WaitingClients.Info.Plural"),size));
				}
			});
		},"ProcessWaitingClientsList").start();
	}

	/**
	 * Befehl: Liste der Kunden aktualisieren
	 */
	private void commandUpdateClientList() {
		if (clientInfo==null) return;

		final int index=listAll.getSelectedIndex();

		final DefaultListModel<ClientInfo> model=new DefaultListModel<>();
		new Thread(()->{
			final List<ClientInfo> list=clientInfo.get();
			if (list!=null) list.forEach(model::addElement);
			SwingUtilities.invokeLater(()->{
				listAll.setModel(model);
				if (index>=0 && index<model.size()) listAll.setSelectedIndex(index);

				if (listAllInfo!=null) {
					final int size=model.size();
					listAllInfo.setText(String.format((size==1)?Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.AllClients.Info.Singular"):Language.tr("Surface.PopupMenu.SimulationStatisticsData.Tab.AllClients.Info.Plural"),size));
				}
			});
		},"ProcessAllClientsList").start();
	}

	/**
	 * Timer-Task zur Aktualisierung der Daten
	 * @see ModelElementAnimationInfoDialog#commandAutoUpdate()
	 */
	private class UpdateTimerTask extends TimerTask {
		@Override
		public void run() {
			if (buttonAutoUpdate.isSelected()) SwingUtilities.invokeLater(()->{
				final boolean isAllClientsTab=(clientInfo!=null && tabs.getSelectedIndex()==tabs.getTabCount()-1);
				if (!isAllClientsTab) commandUpdate();
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
	 * Plant den n�chsten Update-Schritt ein.
	 * @see #commandAutoUpdate()
	 * @see UpdateTimerTask
	 */
	private void scheduleNextUpdate() {
		if (timer!=null) timer.schedule(new UpdateTimerTask(),250);
	}

	/**
	 * Befehl: Daten zu dem gew�hlten (wartenden) Kunden anzeigen
	 * @param list	Liste aus der der aktuell gew�hlte Kunde angezeigt werden soll
	 */
	private void commandShowClientData(final JList<ClientInfo> list) {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final ClientInfo clientInfo=list.getModel().getElementAt(index);
		if (clientInfo==null) return;

		final ModelElementAnimationInfoClientDialog infoDialog=new ModelElementAnimationInfoClientDialog(this,model,clientInfo,list==listWaiting,getRealClient!=null);
		if (!infoDialog.getShowEditorDialog()) return;

		commandEditClientData(list);
	}

	/**
	 * Befehl: Editor zu dem gew�hlten (wartenden) Kunden anzeigen
	 * @param list	Liste aus der der aktuell gew�hlte Kunde bearbeitet werden soll
	 */
	private void commandEditClientData(final JList<ClientInfo> list) {
		final int index=list.getSelectedIndex();
		if (index<0) return;

		final ClientInfo clientInfo=list.getModel().getElementAt(index);
		if (clientInfo==null) return;

		final RunDataClient client=getRealClient.apply(clientInfo.number);
		if (client==null) {
			MsgBox.error(this,Language.tr("Surface.PopupMenu.SimulationStatisticsData.EditClient"),Language.tr("Surface.PopupMenu.SimulationStatisticsData.EditClient.Error"));
			return;
		}

		final ModelElementAnimationEditClientDialog editDialog=new ModelElementAnimationEditClientDialog(this,modelImages,model,client);
		if (editDialog.getClosedBy()==BaseDialog.CLOSED_BY_OK) {
			if (list==listWaiting) commandUpdateWaitingClientList();
			if (list==listAll) commandUpdateClientList();
		}
	}

	@Override
	protected boolean closeButtonOK() {
		if (timer!=null) {timer.cancel(); timer=null;}
		return true;
	}

	/**
	 * Datensatz f�r die Informationen zu einem Kunden
	 */
	public static class ClientInfo {
		/** Benutzerdefinierte Animationsicons */
		private final ModelAnimationImages animationImages;

		/** ID des Kunden */
		public final long id;
		/** Fortlaufende Nummer des Kunden */
		public final long number;
		/** Nummer des Kundentyps */
		public final int typeId;
		/** Name des Kundentyps */
		public final String typeName;
		/** Icon f�r den Kunden */
		public final String icon;
		/** Wurde dieser Kunde in der Warm-up-Phase erstellt? */
		public boolean isWarmUp;
		/** Sollen die Daten zu diesem Kunden in der Statistik erfasst werden? */
		public boolean inStatistics;
		/** Anzahl an Kunden in dem Batch (0, wenn dieser Kunde kein tempor�rer Batch ist) */
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

			id=client.hashCode();
			number=client.clientNumber;

			typeId=client.type;
			typeName=(model==null)?"A":model.clientTypes[client.type];
			icon=(client.icon==null)?"":client.icon;

			isWarmUp=client.isWarmUp;
			inStatistics=client.inStatistics;

			final List<RunDataClient> batchClients=client.getBatchData();
			if (batchClients==null) batch=0; else batch=batchClients.size();

			currentPosition=client.nextStationID;

			waitingTime=client.waitingTime/1000.0;
			transferTime=client.transferTime/1000.0;
			processTime=client.processTime/1000.0;
			residenceTime=client.residenceTime/1000.0;

			waitingCosts=client.waitingAdditionalCosts;
			transferCosts=client.transferAdditionalCosts;
			processCosts=client.processAdditionalCosts;

			clientData=new HashMap<>();
			for (int i=0;i<=client.getMaxUserDataIndex();i++) {
				final double value=client.getUserData(i);
				if (value!=0.0) clientData.put(i,value);
			}

			clientTextData=new HashMap<>();
			for (String key: client.getUserDataStringKeys()) clientTextData.put(key,client.getUserDataString(key));

			path=client.getPath();
		}

		/**
		 * Erzeugt ein Label zur Anzeige in der Liste zu diesem Kundeninfo-Datensatz
		 * @param images	Objekt welches die Icons f�r die Animation vorh�lt
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
			text.append("id=<b>");
			text.append(id);
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
		 * Liefert das Icon f�r den Kunden.
		 * @param images	Objekt welches die Icons f�r die Animation vorh�lt
		 * @return	Icon f�r den Kunden
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
	 * Renderer f�r die Eintr�ge der Liste der (wartenden) Kunden
	 * @see ModelElementAnimationInfoDialog#listWaiting
	 * @see ModelElementAnimationInfoDialog#listAll
	 */
	public static class JClientInfoRender implements ListCellRenderer<ClientInfo> {
		/** Objekt welches die Icons f�r die Animation vorh�lt */
		private final AnimationImageSource images;
		/** Handelt es sich um wartende Kunden? */
		private final boolean isWaitingClientsList;

		/**
		 * Konstruktor der Klasse
		 * @param images	Objekt welches die Icons f�r die Animation vorh�lt
		 * @param isWaitingClientsList	Handelt es sich um wartende Kunden?
		 */
		public JClientInfoRender(final AnimationImageSource images, final boolean isWaitingClientsList) {
			this.images=images;
			this.isWaitingClientsList=isWaitingClientsList;
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends ClientInfo> list, ClientInfo value, int index, boolean isSelected, boolean cellHasFocus) {
			final JLabel label=value.buildLabel(images,isWaitingClientsList);

			if (isSelected) {
				label.setBackground(list.getSelectionBackground());
				label.setForeground(list.getSelectionForeground());
				label.setOpaque(true);
			} else {
				label.setBackground(list.getBackground());
				label.setForeground(list.getForeground());
				label.setOpaque(false);
			}
			return label;
		}
	}
}